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
import bankapi

import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__),'thirdparty') )

import time
import feencryptor

from datetime import datetime
app = flaskInit.app

#rncryptor = RNCryptor.RNCryptor()

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
        if not isSender and data[0]=='t' :
            return decryptToken('receivertokens', data)

        return data;
        #if method == 'pan' : return data
        #if method == 'ignore' : return ''
        #raise Exception("Unknown method %s" % method)


@app.route("/verification-result", methods=['GET'])
def verifyres() :
    success = flask.request.args.get('success','false') == 'true'
    return flask.Response('Success' if success else 'Error',200 if success else 400)
    
def gotoVerificationResult(success, transactionid = '') :
    return flask.redirect(flask.url_for('verifyres', success='true' if success else 'false', transactionid=transactionid))

@app.route("/verification-complete", methods=['POST'])
def verifycomplete() :
    try :
        data = zlib.decompress(base64.b64decode(flask.request.form['PaRes']))
        
        root = xml.etree.ElementTree.fromstring(data)
        result = root.findall('Message')[0].findall('PARes')[0].findall('TX')[0].findall('status')[0].text
        
        token = flask.request.args.get('token','')
        
        cursor = feeasyMySQL.getCursor()
        cursor.execute("SELECT termurl, postdata, url, cookies, apiclass FROM verifications WHERE token=%(token)s", {'token': token})
        
        sqlResult = cursor.fetchone()
        if sqlResult is None :
            raise Exception()
            
        termurl, postdata, ascurl, cookies, apiClassId = sqlResult
        
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
        data = resp.read()

        error, trunsactionId = apiClass.getTransactionResult(resp)

    except Exception as e:
        
        error = True
    
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
    bankClass = bankapi.apiAlfaWeb

    if method == 'transfer' :
        result = bankClass.transfer(payData)
        if result['error'] : return flask.jsonify(error = True)

        queryId = result['queryId']
        cursor = feeasyMySQL.getCursor()
        cursor.execute("INSERT INTO verifications (token, url, postdata, date, termurl, cookies, apiclass) VALUES (%(token)s, %(url)s, %(postdata)s, NOW(), %(termUrl)s, %(cookies)s, %(api)s)",
            {
                'token' : queryId.hex,
                'url'   : result['url'],
                'postdata'  : json.dumps(result['data']),
                'termUrl' : result['termUrl'],
                'cookies' : result['cookies'],
                'api'  : bankClass.ID
            })

        return flask.jsonify(error = False, token=queryId.hex,
                             url=flask.url_for('verification', _external=True) + '?' +
                             urllib.urlencode({'token' : queryId.hex}))
    elif method == 'check' :
        result = bankClass.getFee(payData)
        if result['error'] : return flask.jsonify(error = True)

        senderCard = cardFormatter.CardNumber(payData.senderCard)

        return flask.jsonify( error = False,
                              fee=result['fee'],
                              bank=bankClass.getBankData(),
                              sender_card=senderCard.prettify(),
                              sender_card_type=senderCard.getType().name )

    return flask.Response('Bad Request',400)

@app.route("/tokengen", methods=['GET', 'POST'])
def generateToken() :
    data = flask.request.form if flask.request.method=='POST' else flask.request.args
    pan = data.get('pan', '')

    if not pan.isdigit() :
        return flask.jsonfy(
            error = True,
            reason = "pan must be numeric"
        )

    id, cypher, cyphertoken = createTokenCypher('receivertokens', pan)

    return flask.jsonify(
        error = False,
        cyphertoken = cyphertoken,
        cypher = cypher,
        id = str(makeTokenId(id)),
        forpan = str(decryptToken('receivertokens', cyphertoken))
    )

def decryptToken(table, cyphertoken) :
    if cyphertoken[0]!='t' : raise Exception('Bad token format')

    token  = cyphertoken[1:32+1]
    cypher = cyphertoken[32+1:]

    cursor = feeasyMySQL.getCursor()
    cursor.execute("SELECT data FROM " + table + " WHERE token=%s", [token])

    sqlResult = cursor.fetchone()
    if sqlResult is None :
        return None

    return feencryptor.decryptPan10(cypher, sqlResult[0])

def createTokenCypher(table, pan) :
    id, token = createToken(table)

    startTime = time.time()
    cypher, data = feencryptor.encryptPan10(pan)
    print "Cypher time %s" % str(time.time() - startTime)

    cursor = feeasyMySQL.getCursor()
    cursor.execute("UPDATE " + table + " SET data=%s WHERE token=%s", [data, token.hex])

    return id, cypher, "t%s%s" % (token.hex, cypher)

#tokenField in table must be CHAR(32) UNIQUE
def createToken(table) :
    while True:
        try :
            token = uuid.uuid4()

            cursor = feeasyMySQL.getCursor()
            cursor.execute("INSERT INTO " + table + " (token) VALUES (%s)" , [token.hex])

            if cursor.rowcount > 0 :
                return cursor.lastrowid, token
        except : pass # exception if collision detected


def luhn_checksum(card_number):
    def digits_of(n):
        return [int(d) for d in str(n)]
    digits = digits_of(card_number)
    odd_digits = digits[-1::-2]
    even_digits = digits[-2::-2]
    checksum = 0
    checksum += sum(odd_digits)
    for d in even_digits:
        checksum += sum(digits_of(d*2))
    return checksum % 10

def makeTokenId(id):
    if id > 999999999999:
        return -1

    token = 4687390000000000000 + 10*id;

    checksum = luhn_checksum(token)

    if checksum == 0:
        token = token
    else:
        token = token + 10 - checksum

    return token

if __name__ == '__main__':
    app.run(debug=True, host='192.168.157.21')