# -*- coding: utf-8 -*-

import json, httplib, flask, uuid, urlparse
from datetime import datetime

class BankApi :
    allApiFunction = {}


class AlfaWebEmulation (BankApi) :
    ID = 'alfaweb'
    def __init__(self) :
        BankApi.allApiFunction[AlfaWebEmulation.ID] = self

    def transfer(self, payData):
        result = self.getFee(payData)
        if result['error'] :
            return result

        sum = payData.sumCents - result['fee']

        params = {
            "sender_type":"cnm",
            "sender_value":str(payData.senderCard.pan),
            "recipient_type":"cnm",
            "recipient_value":str(payData.recipientCard.pan),
            "exp_date":"%d%02d%02d" % (datetime.now().year // 100, payData.senderExpYear, payData.senderExpMonth),
            "cvv":"%03d" % payData.senderCSC,
            "amount": str(sum),
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

        queryId = uuid.uuid4()

        return {
            'url' : data['acsURL'],
            'error' : False,
            'termUrl' : 'https://click.alfabank.ru/api/v1/transfers',#data['termURL'],
            'queryId' : queryId,
            'cookies' : resp.getheader('Set-Cookie'),
            'data' : {
                'PaReq' : data['pareq'].replace('\n',''),
                'MD'    : data['md'],
                'TermUrl' : flask.url_for('verifycomplete', _external=True, token=queryId.hex) }
        }

    def getBankData(self):
        return {
            'id': 'alfa',
            'api_id' : AlfaWebEmulation.ID,
            'name-ru': u'Альфа-Банк',
            'web-site': 'http://alfabank.ru/',
        }

    def getFee(self, payData):
        params = {
            "sender_type":"cnm",
            "sender_value":str(payData.senderCard.pan),
            "recipient_type":"cnm",
            "recipient_value":str(payData.recipientCard.pan),
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
            
        fee = int(data['fee'])

        return {'error': False, 'fee': fee, 'sum': payData.sumCents, 
        'fee2': fee, 'sum2': payData.sumCents + fee}

    def getTransactionResult(self, response) :
        try :
            result = urlparse.parse_qs(urlparse.urlparse(response.getheader('Location')).query)
            if 'error' in result and result['error'][0]=='true' : return True, result['transactionId'][0]
            return False, result['transactionId'][0]
        except :
            return True, ''

apiAlfaWeb = AlfaWebEmulation()