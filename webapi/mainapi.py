# -*- coding: utf-8 -*-

import flask
import json
import httplib
import cgi
import urlparse
import urllib

import zlib
import base64
import xml.etree.ElementTree

import mysql.connector

import flaskInit
import feeasyMySQL
import bankapi

import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__),'thirdparty') )

import feetoken
import cardFormatter
import qrmailer

from datetime import datetime
app = flaskInit.app

class ErrCode :
    def __init__(self, name, number):
        self.name = name
        self.number = number

ErrCode.success = ErrCode("ERR_SUCCESS", 0)
ErrCode.dataError = ErrCode("ERR_DATA_ERROR", 100010)
ErrCode.senderTokenNotFound = ErrCode("ERR_SENDER_TOKEN_NOT_FOUND", 100020)
ErrCode.receiverTokenNotFound = ErrCode("ERR_RECEIVER_TOKEN_NOT_FOUND", 100030)
ErrCode.apiId = ErrCode("ERR_API_ID", 100040)
ErrCode.bankError = ErrCode("ERR_BANK_ERROR", 100050)

class PayData :
    def __init__(self,
                 senderCardToken, recipientCardToken,
                 senderExpYear, senderExpMonth,
                 senderCSC, sumCents, recipientFee, senderMessage):
        self.senderCard     = feetoken.PanToken(senderCardToken, True)
        self.recipientCard  = feetoken.PanToken(recipientCardToken, False)

        self.senderExpYear  = senderExpYear
        self.senderExpMonth = senderExpMonth

        self.senderCSC = senderCSC
        self.sumCents = sumCents

        self.recipientFee = recipientFee
        self.senderMessage = senderMessage

    def checkCorrectness(self, needSenderCard = False, needRecipientCard = False, needExpDat = False, needSum = False, needCsc = False):
        if needSenderCard and not self.senderCard.isSet() : return True, "Карта отправителя не задана", ErrCode.dataError
        if needRecipientCard and not self.recipientCard.isSet() : return True, "Карта получателя не задана", ErrCode.dataError
        if needExpDat and not self.isExpDateSet() : return True, "Дата окончания не задана", ErrCode.dataError
        if needSum and not self.isSumSet() : return True, "Сумма не задана", ErrCode.dataError
        if needCsc and not self.isCscSet() : return True, "Код CSC не задан", ErrCode.dataError

        if self.senderCard.error    : return True, "Ошибка карты отправителя: %s" % self.senderCard.errorMessage, ErrCode.senderTokenNotFound
        if self.recipientCard.error : return True, "Ошибка карты получателя: %s"  % self.recipientCard.errorMessage, ErrCode.receiverTokenNotFound

        if self.isExpDateSet() and not str(self.senderExpYear).isdigit()  : return True, "Год должен быть числом", ErrCode.dataError
        if self.isExpDateSet() and not str(self.senderExpMonth).isdigit() : return True, "Месяц должен быть числом", ErrCode.dataError
        if self.isCscSet() and not str(self.senderCSC).isdigit()      : return True, "Код CSC должен быть числом", ErrCode.dataError
        if self.isSumSet() and not str(self.sumCents).isdigit()       : return True, "Сумма перевода должна быть числом", ErrCode.dataError

        if self.isExpDateSet() : self.senderExpYear  = int(self.senderExpYear)
        if self.isExpDateSet() : self.senderExpMonth = int(self.senderExpMonth)
        if self.isCscSet() : self.senderCSC      = int(self.senderCSC)
        if self.isSumSet() : self.sumCents       = int(self.sumCents)

        return False, "Ok", ErrCode.success

    @staticmethod
    def valueIsSet(value):
        return value is not None and value != '' and value != '-'

    def isExpDateSet(self):
        return PayData.valueIsSet(self.senderExpMonth) and PayData.valueIsSet(self.senderExpYear)

    def isSumSet(self):
        return PayData.valueIsSet(self.sumCents)

    def isCscSet(self):
        return PayData.valueIsSet(self.senderCSC)

@app.route("/verification-result", methods=['GET'])
def verifyres() :
    success = flask.request.args.get('success','false') == 'true'
    return flask.Response('Success' if success else 'Error',200 if success else 400)
    
def gotoVerificationResult(success, transactionid = '') :
    return flask.redirect(flask.url_for('verifyres', success='true' if success else 'false', transactionid=transactionid))

@app.route("/verification-complete", methods=['POST'])
def verifycomplete() :
    pares = ''
    success1, success2 = False, False
    historyId = -1
    try :
        token = flask.request.args.get('token','')
        
        cursor = feeasyMySQL.getCursor()
        cursor.execute("SELECT termurl, postdata, url, cookies, apiclass, historyId, payerToken FROM verifications WHERE token=%(token)s", {'token': token})
        
        sqlResult = cursor.fetchone()
        if sqlResult is None :
            raise Exception()

        termurl, postdata, ascurl, cookies, apiClassId, historyId, payerToken = sqlResult
        
        pares = zlib.decompress(base64.b64decode(flask.request.form['PaRes']))
        
        root = xml.etree.ElementTree.fromstring(pares)
        result = root.findall('Message')[0].findall('PARes')[0].findall('TX')[0].findall('status')[0].text
        success1 = result=='Y'
        
        content = urllib.urlencode(dict([[x,flask.request.form[x]] for x in flask.request.form]))
        urlObj = urlparse.urlparse(termurl)
        apiClass = bankapi.BankApi.allApiFunction[apiClassId]

        headers = {
            "Accept":"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Accept-Encoding":"gzip, deflate",
            "Accept-Language":  "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4",
            "Cache-Control": "max-age=0",
            "Connection":  "keep-alive",
            "Content-Length":  str(len(content)),
            "Host":  urlObj.hostname,
            "Origin":  flask.request.headers.get('Origin') or "%s://%s" % (urlparse.urlparse(ascurl).scheme,urlparse.urlparse(ascurl).netloc),
            "Referer":  flask.request.headers.get('Referer') or ascurl,
            "User-Agent":  "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36",
            "Content-Type":  "application/x-www-form-urlencoded",
            "Accept":  "application/json, text/plain, */*",
            "Cookie": cookies,
        }

        con = httplib.HTTPSConnection(urlObj.hostname)
        con.connect()
        con.request('POST', urlObj.path, content, headers)

        resp = con.getresponse()

        error, trunsactionId = apiClass.getTransactionResult(resp)
        success2 = not error

        if success1 and not success2 and payerToken is not None and payerToken!='' :
            cursor = feeasyMySQL.getCursor()
            cursor.execute('UPDATE payertokens SET failAttempts=failAttempts+1, active=failAttempts<4 WHERE token=%(token)s',
                {'token':payerToken})

        #print "WebApi: Error 3d: %s, bank %s" % (result!='Y', error)

    except Exception as e:
        print "WebApi: " + e
        trunsactionId = ''
        error = True

    # save to hist:
    if historyId>=0 :
        cursor = feeasyMySQL.getCursor()
        cursor.execute("UPDATE transactionhistory SET "
                   "confirmDate=NOW(), "
                   "pares=%(pares)s, "
                   "success3d=%(success1)s, "
                   "successBank=%(success2)s, "
                   "transactionId=%(tid)s"
                   " WHERE id=%(id)s",
                   {'pares': pares, 'success1': success1, 'success2': success2, 'tid': trunsactionId, 'id': historyId})
    
    return gotoVerificationResult(not error, trunsactionId)

@app.route("/verification", methods=['GET'])
def verification() :
    try :
        token = flask.request.args.get('token','')
        if len(token) != 32 :
            return flask.Response('Bad Request',400)

        cursor = feeasyMySQL.getCursor()
        cursor.execute("DELETE FROM verifications WHERE date < DATE_ADD(NOW(),INTERVAL -1 DAY)")

        cursor = feeasyMySQL.getCursor()
        cursor.execute("SELECT url, postdata FROM verifications WHERE token=%(token)s", {'token': token})

        result = cursor.fetchone()
        if result is None :
            raise Exception()
            #return flask.Response('Bad Request',400)

        url, postdata = result
        data = json.loads(postdata)

        return \
            """
            <html>
                <head>
                    <title>Redirecting to verification...</title>
                    <script>
                        window.onload=function() {
                            var form = document.getElementById('postform');
                            form.submit();
                        }
                    </script>
                </head>
                <body>
                    <form id="postform" method="POST" action="%s" target="_self">
                        %s
                    </form>
                </body>
            </html>""" % \
               (url, '\n        '.join(['<input type="hidden" value="%s" name="%s" />' % (cgi.escape(data[x]), cgi.escape(x)) for x in data]))
    except :
        return gotoVerificationResult(False)

def createHistId() :
    cursor = feeasyMySQL.getCursor()
    cursor.execute("INSERT INTO transactionhistory VALUES ()")
    return cursor.lastrowid

@app.route("/payapi", methods=['GET', 'POST'])
def payapi() :
    data = flask.request.form if flask.request.method=='POST' else flask.request.args

    senderCard    = data.get('sender_card','-')
    recipientCard = data.get('recipient_card','-')

    payData = PayData(
        senderCard,
        recipientCard,
        data.get('sender_exp_year', ''),
        data.get('sender_exp_month', ''),
        data.get('sender_csc', ''),
        data.get('sum', '-'),
        data.get('recipientfee', 'y') == 'y',
        data.get('sender_message', '')[0:2000]
    )

    method = data.get('method')
    api_id = data.get('api_id', None)
    
    if api_id is None :
        api_id = 'alfaweb'
    
    if not api_id in bankapi.BankApi.allApiFunction :
        error, message = True, "Некорректный api_id"
        return flask.jsonify(error = True, reason = message )
    
    bankClass = bankapi.BankApi.allApiFunction[api_id]
    
    historyId = None
    if 'history_id' in data and data['history_id'] != '' and data['history_id'].isdigit() and 'user_id' in data:
        historyId = int(data['history_id'])
        cursor = feeasyMySQL.getCursor()
        cursor.execute("SELECT EXISTS (SELECT * FROM transactionhistory WHERE id=%(id)s AND userId=%(user)s)",
                       {'id':historyId, 'user' : data['user_id']})
        if not cursor.fetchone()[0] : historyId = None

    if historyId is None :
        historyId = createHistId()

    if method == 'transfer' :
        if not 'api_id' in data :
            error, message, errCode = True, "Необходимо явно указывать api_id", ErrCode.apiId
        else : error, message, errCode = payData.checkCorrectness(
            needSenderCard = True, needRecipientCard = True, needExpDat = True, needSum = True, needCsc = True
        )
        if error :
            return flask.jsonify(error = True, reason = message, err_name = errCode.name )

        result = bankClass.transfer(payData)
        if result['error'] :
            return flask.jsonify(error = True, reason = result['error-description'], err_name = ErrCode.bankError.name)
            
        payertoken = ''
        if senderCard[0]=='t' :
            cyphertoken = senderCard
        else :
            id, cypher, cyphertoken = feetoken.createTokenCypher('payertokens', payData.senderCard.pan)

        if cyphertoken[0]=='t' :
            payertoken = cyphertoken[1:17]

        cursor = feeasyMySQL.getCursor()
        cursor.execute("UPDATE transactionhistory SET "
                       "recipientToken = %(recipientToken)s, "
                       "transferDate = NOW(), "
                       "sum = %(sum)s, "
                       "fee = %(fee)s, "
                       "api = %(api)s, "
                       "payerCardMask = %(payerCardMask)s, "
                       "payerToken = %(payerToken)s, "
                       "senderMessage = %(senderMessage)s"
                       " WHERE id=%(id)s",
            {
                'recipientToken' : recipientCard[1:16+1],
                'payerToken' : payertoken,
                'payerCardMask' : cardFormatter.CardNumber(payData.senderCard.pan).prettify(),
                'senderMessage' : payData.senderMessage,
                'sum'   : result['sum'],
                'fee'   : result['fee'],
                'api'   : bankClass.ID,
                'id'    : historyId
            })

        queryId = result['queryId']
        cursor = feeasyMySQL.getCursor()
        cursor.execute("INSERT INTO verifications (token, url, postdata, date, termurl, cookies, apiclass, historyId, payerToken) VALUES "
                       "(%(token)s, %(url)s, %(postdata)s, NOW(), %(termUrl)s, %(cookies)s, %(api)s, %(historyId)s, %(payerToken)s)",
            {
                'token' : queryId.hex,
                'url'   : result['url'],
                'postdata'  : json.dumps(result['data']),
                'termUrl' : result['termUrl'],
                'cookies' : result['cookies'],
                'api'  : bankClass.ID,
                'historyId' : historyId,
                'payerToken' : payertoken
            })

        return flask.jsonify(error = False, token=queryId.hex,
                             cyphertoken = cyphertoken,
                             url=flask.url_for('verification', _external=True) + '?' +
                             urllib.urlencode({'token' : queryId.hex}))
    elif method == 'check' :
        error, message, errCode = payData.checkCorrectness(needRecipientCard = True)
        if error :
            return flask.jsonify(error = True, reason = message, err_name = errCode.name)

        response = {
            'error': False,
            'message': payData.recipientCard.data['descr']
        }

        if payData.senderCard.isSet() :
            senderCard = payData.senderCard.cardNumber
            response['sender_card'] = senderCard.prettify()
            response['sender_card_type'] = senderCard.getType().name

            if payData.isSumSet() :
                result = bankClass.getFee(payData)
                if result['error'] :
                    return flask.jsonify(error = True, reason = result['error-description'], err_name = ErrCode.bankError.name)

                response['fee'] = result['fee']
                response['sum'] = result['sum']
                response['fee2'] = result['fee2']
                response['sum2'] = result['sum2']
                
                response['bank'] = bankClass.getBankData()

        if 'user_id' in data :
            cursor = feeasyMySQL.getCursor()
            cursor.execute("UPDATE transactionhistory SET "
                           "checkDate=NOW(), "
                           "recipientToken=%(recipientToken)s, "
                           "userId=%(user)s"
                           " WHERE id=%(id)s",
                {
                    'recipientToken' : recipientCard[1:16+1],
                    'user': data['user_id'],
                    'id': historyId
                })

        response['history_id'] = historyId

        return flask.jsonify( **response )

    return flask.Response('Bad Request',400)

@app.route("/tokengen", methods=['GET', 'POST'])
def generateToken() :
    data = flask.request.form if flask.request.method=='POST' else flask.request.args

    pan = data.get('pan', '')
    mail = data.get('e-mail', '')
    descr = data.get('descr', '')

    if not pan.isdigit() :
        return flask.jsonify(
            error = True,
            reason = "pan must be numeric"
        )

    if len(pan) > 24 :
        return flask.jsonify(
            error = True,
            reason = "pan possible length up to 24 digit"
        )

    id, cypher, cyphertoken = feetoken.createTokenCypher('receivertokens', pan, {'mail': mail, 'descr': descr})

    return flask.jsonify(
        error = False,
        cyphertoken = cyphertoken,
        cypher = cypher,
        id = str(feetoken.makeTokenId(id)),
        forpan = str(feetoken.decryptToken('receivertokens', cyphertoken)[0])
    )
    
@app.route("/join", methods=['GET', 'POST'])
def joinPage() :
    if flask.request.method=='GET' :
        content = """
<html><head><script src='https://www.google.com/recaptcha/api.js?hl=ru'></script></head>
<body>
    <form method="POST" action="%s">
        <div><input type="text" name="email" placeholder="e-mail"></div>
        <div><input type="text" name="card"  placeholder="card"></div>
        <div><input type="text" name="message"  placeholder="message"></div>

        <div class="g-recaptcha" data-sitekey="6Lee2P4SAAAAANmLmevRTR_ei92o2NA0EsIyoXWh"></div>
        
        <div><input type="submit"></div>
    </form>
</body>
</html>
        """ % flask.url_for('joinPage')
        
        return content
    elif flask.request.method=='POST' :
        catchaSecret = '6Lee2P4SAAAAAOcUACVTYo5Uj0M7dM8DXN4v3jPB'
        catchaResponse = flask.request.form.get('g-recaptcha-response')

        con = httplib.HTTPSConnection('www.google.com')
        con.connect()
        con.request('GET', '/recaptcha/api/siteverify?' + urllib.urlencode({'secret': catchaSecret, 'response': catchaResponse}))

        resp = con.getresponse()
        data = json.loads(resp.read())

        #if not data['success'] :
        #    return flask.jsonify(error=True, errorMessage='Капча введена неверно')

        pan = flask.request.form.get('card').replace(' ', '')
        mail = flask.request.form.get('email')
        message = flask.request.form.get('message')

        #detect shortmessage
        ellipsis = u"…"
        ellipsisSize = len(ellipsis.encode('utf-8'))

        if len(message.encode('utf-8')) <= qrmailer.MESSAGE_MAX_LEN :
            shortMessage = message
        else :
            for i in range(len(message)) :
                if len(message[0:i].encode('utf-8')) + ellipsisSize > qrmailer.MESSAGE_MAX_LEN : break
            shortMessage = message[0:i]+ellipsis

        if not pan.isdigit() or len(pan)>24 :
            return flask.jsonify(error=True, errorMessage='Неверный номер карты')

        id, cypher, cyphertoken = feetoken.createTokenCypher('receivertokens', pan, {'mail': mail, 'descr': message})

        number = cardFormatter.CardNumber(pan)

        qrmailer.processToken(shortMessage,cyphertoken,number.getType().name,number.prettify(),mail)

        return flask.jsonify(error=False, errorMessage='OK')

if __name__ == '__main__':
    #app.run(debug=True, host='37.252.124.233')
    app.run(debug=True, host='192.168.157.17')