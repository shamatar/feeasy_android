# -*- coding: utf-8 -*-

from Crypto import Random
from cardFormatter import CardNumber

import time
import feencryptor
import mysql.connector
import feeasyMySQL

class PanToken :
    ERR_SUCCESS = 0
    ERR_TOKEN_NOT_FOUND = 200010
    ERR_TOKEN_REMOVED = 200020
    ERR_BAD_PAN = 200030

    def __init__(self, data, isSender) :
        self.error = False
        self.errCode = PanToken.ERR_SUCCESS

        self.token = data
        self.isSender = isSender
        
        self.pan = None

        if not self.isSet() :
            return

        if data[0] == 't' :
            self.table = 'payertokens' if isSender else 'receivertokens'
            self.pan, self.data = decryptToken(self.table, data)
        else :
            if not isSender :
                self.error, self.errorMessage = True, "неизвестный формат получателя"
                self.errCode = PanToken.ERR_TOKEN_NOT_FOUND
                return

            if data.isdigit() :
                self.pan = data

            self.data = {}

        if self.pan is None :
            self.error = True
            self.errorMessage = "неверный токен"
            self.errCode = PanToken.ERR_TOKEN_NOT_FOUND

        if 'active' in self.data and not self.data['active'] :
            self.pan = None
            self.error = True
            self.errorMessage = "токен удалён"
            self.errCode = PanToken.ERR_TOKEN_REMOVED

            return

        self.error, self.errorMessage, self.errCode = self.isError()

        if not self.error :
            self.cardNumber = CardNumber(self.pan)

    def isSet(self):
        return not(self.token is None or len(self.token) == 0 or self.token=='-')

    def isError(self) :
        if not self.isSet() : return False, "Not set", PanToken.ERR_BAD_PAN
        if self.pan is None : return True, self.errorMessage, PanToken.ERR_BAD_PAN
        if len(str(self.pan)) == 0 : return True, "номер карты не задан", PanToken.ERR_BAD_PAN
        if not str(self.pan).isdigit()    : return True, "номер карты должен быть числовым", PanToken.ERR_BAD_PAN
        if len(str(self.pan)) > 24 : return True, "номер карты слишком длинный", PanToken.ERR_BAD_PAN
        if len(str(self.pan)) < 9  : return True, "номер карты слишком короткий", PanToken.ERR_BAD_PAN

        return False, "Ok", PanToken.ERR_SUCCESS

def decryptToken(table, cyphertoken) :
    if cyphertoken[0]!='t' : raise Exception('Bad token format')

    token  = cyphertoken[1:16+1]
    cypher = cyphertoken[16+1:]

    cursor = feeasyMySQL.getCursor()
    cursor.execute("SELECT * FROM " + table + " WHERE token=%s", [token])

    sqlResult = cursor.fetchone()
    if sqlResult is None :
        return None, "token not found"

    data = dict(zip(cursor.column_names, sqlResult))

    pan = feencryptor.decryptPan10(cypher, data['data'])

    return pan, data

def createTokenCypher(table, pan, fields = {}) :

    startTime = time.time()
    cypher, data = feencryptor.encryptPan10(pan)
    print "Cypher time %s" % str(time.time() - startTime)

    #cursor = feeasyMySQL.getCursor()
    #cursor.execute("UPDATE " + table + " SET data=%s WHERE token=%s", [data, token.hex])

    fields = fields.copy()
    fields['data'] = data

    id, token = createToken(table, fields)

    return id, cypher, "t%s%s" % (token, cypher)

#tokenField in table must be CHAR(32) UNIQUE
def createToken(table, fields = {}) :
    for attempt in range(10) :
        try :
            token = Random.new().read(8).encode('hex')
            #don't allow first byte be zero
            if token[0:2] == '00' : continue

            cursor = feeasyMySQL.getCursor()

            cursor.execute("INSERT INTO %s (%s) VALUES (%s)" % (
                table, ','.join(['token'] + fields.keys()), ','.join(['%s'] * (1 + len(fields)))
                                                               ) , [token] + fields.values())

            if cursor.rowcount > 0 :
                return cursor.lastrowid, token
        except mysql.connector.errors.DatabaseError as e:
            if e.errno!=mysql.connector.errorcode.ER_DUP_ENTRY:
                break

    raise Exception('token creation error')


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