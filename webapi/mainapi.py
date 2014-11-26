# -*- coding: utf-8 -*-

import flask
import json
import httplib
import uuid
import cgi
import urlparse
import urllib

import zlib
import base64
import xml.etree.ElementTree

import flaskInit
import feeasyMySQL
import cardFormatter

from datetime import datetime

app = flaskInit.app


class PayData:
    def __init__(self, senderCard, recipientCard, senderExpYear, senderExpMonth, senderCSC, sumCents ):
        self.senderCard = senderCard
        self.recipientCard = recipientCard
        self.senderExpYear = senderExpYear
        self.senderExpMonth = senderExpMonth
        self.senderCSC = senderCSC
        self.sumCents = sumCents

    @staticmethod
    def getCardNumber(data, isSender):
        return data;
        #if method == 'pan' : return data
        #if method == 'ignore' : return ''
        #raise Exception("Unknown method %s" % method)


class AlfaWebEmulation :
    @staticmethod
    def transfer(payData):
        params = {
            "sender_type":"cnm",
            "sender_value":payData.senderCard,
            "recipient_type":"cnm",
            "recipient_value":payData.recipientCard,
            "exp_date":"%d%02d%02d" % (datetime.now().year // 100, payData.senderExpYear, payData.senderExpMonth),
            "cvv":"%03d" % payData.senderCSC,
            "amount": str(payData.sumCents),
            "currency":"RUR",
            "client_ip":"127.0.0.1" }

        content = json.dumps(params)

        headers = {
            "Authorization":  "Bearer bd5970b9-ba7c-4d06-a0fd-b8c5bed02e40",
            "Origin":  "https://alfabank.ru",
            "Accept-Encoding":  "gzip, deflate, sdch",
            "Host":  "click.alfabank.ru",
            "Accept-Language":  "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4",
            "User-Agent":  "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36",
            "Content-Type":  "application/json;charset=UTF-8",
            "Accept":  "application/json, text/plain, */*",
            "Referer":  "https://alfabank.ru/retail/cardtocard/alfaperevod/",
            "Connection":  "keep-alive",
            "Content-Length":  str(len(content)),
        }

        con = httplib.HTTPSConnection('click.alfabank.ru')
        con.connect()
        con.request('PUT', '/api/v1/transfers', content, headers)

        resp = con.getresponse()
        data = json.loads(resp.read())

        if 'error' in data and data['error'] != '0' :
            return {'error' : True, 'error-description' : 'Error processing request'}

        return {
            'url' : data['acsURL'],
            'error' : False,
            'data' : {
                'PaReq' : data['pareq'].replace('\n',''),
                'MD'    : data['md'],
                'TermUrl' : flask.url_for('verifycomplete', _external=True) }
        }

    @staticmethod
    def getBankData():
        return {
            'id': 'alfa',
            'name-ru': u'Альфа-Банк',
            'web-site': 'http://alfabank.ru/',
        }

    @staticmethod
    def getFee(payData):
        params = {
            "sender_type":"cnm",
            "sender_value":payData.senderCard,
            "recipient_type":"cnm",
            "recipient_value":payData.recipientCard,
            "amount": str(payData.sumCents),
            "currency":"RUR" }

        content = json.dumps(params)

        headers = {
            "Authorization":  "Bearer bd5970b9-ba7c-4d06-a0fd-b8c5bed02e40",
            "Origin":  "https://alfabank.ru",
            "Accept-Encoding":  "gzip, deflate, sdch",
            "Host":  "click.alfabank.ru",
            "Accept-Language":  "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4",
            "User-Agent":  "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36",
            "Content-Type":  "application/json;charset=UTF-8",
            "Accept":  "application/json, text/plain, */*",
            "Referer":  "https://alfabank.ru/retail/cardtocard/alfaperevod/",
            "Connection":  "keep-alive",
            "Content-Length":  str(len(content)),
        }

        con = httplib.HTTPSConnection('click.alfabank.ru')
        con.connect()
        con.request('POST', '/api/v1/fee', content, headers)

        resp = con.getresponse()
        data = json.loads(resp.read())
        
        if 'error' in data and data['error'] != '0':
            return {'error': True, 'error-description': 'Error processing request'}

        return {'error': False, 'fee': int(data['fee'])}

@app.route("/verification-result", methods=['GET'])
def verifyres() :
    success = flask.request.args.get('success','false') == 'true'
    return flask.Response('Success' if success else 'Error',200 if success else 400)
    
def gotoVerificationResult(result) :
    return flask.redirect(flask.url_for('verifyres', success='true' if result else 'false'))

@app.route("/verification-complete", methods=['POST'])
def verifycomplete() :
    try :
        data = zlib.decompress(base64.b64decode(flask.request.form['PaRes'][0]))
        
        root = xml.etree.ElementTree.fromstring(x)
        result = root.findall('Message')[0].findall('PARes')[0].findall('TX')[0].findall('status')[0].text
    except :
        result = 'N'
    
    return gotoVerificationResult(result=='Y')

@app.route("/verification", methods=['GET'])
def verification() :
    try :
        token = flask.request.args.get('token','')
        if len(token) != 32 :
            return flask.Response('Bad Request',400)

        cursor = feeasyMySQL.getCursor()
        cursor.execute("DELETE FROM verifications WHERE date < DATE_ADD(NOW(),INTERVAL -30 MINUTE)")

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

@app.route("/payapi", methods=['GET', 'POST'])
def payapi() :
    data = flask.request.form if flask.request.method=='POST' else flask.request.args

    #cursor = getMySQLCursor()
    #cursor.execute('CREATE TABLE verifications (token VARCHAR(64) UNIQUE NOT NULL PRIMARY KEY, url TEXT, postdata TEXT, date DATETIME, INDEX(date))')

    payData = PayData(
        PayData.getCardNumber(data.get('sender_card','-')   , True),
        PayData.getCardNumber(data.get('recipient_card','-'), False),
        int(data.get('sender_exp_year', '0')),
        int(data.get('sender_exp_month', '0')),
        int(data.get('sender_csc', '0')),
        int(data.get('sum', '0'))
    )

    method = data.get('method')
    if method == 'transfer' :
        result = AlfaWebEmulation.transfer(payData)
        if result['error'] : return flask.jsonify(error = True)

        queryId = uuid.uuid4()
        cursor = feeasyMySQL.getCursor()
        cursor.execute("INSERT INTO verifications (token, url, postdata, date) VALUES (%(token)s, %(url)s, %(postdata)s, NOW())",
            {
                'token' : queryId.hex,
                'url'   : result['url'],
                'postdata'  : json.dumps(result['data'])
            })

        return flask.jsonify(error = False, token=queryId.hex,
                             url=flask.url_for('verification', _external=True) + '?' +
                             urllib.urlencode({'token' : queryId.hex}))
    elif method == 'check' :
        bankClass = AlfaWebEmulation
        result = bankClass.getFee(payData)
        if result['error'] : return flask.jsonify(error = True)

        senderCard = cardFormatter.CardNumber(payData.senderCard)

        return flask.jsonify( error = False,
                              fee=result['fee'],
                              bank=bankClass.getBankData(),
                              sender_card=senderCard.prettify(),
                              sender_card_type=senderCard.getType().name )

    return flask.Response('Bad Request',400)

if __name__ == '__main__':
    app.run(debug=True, host='192.168.157.21')