from Crypto.Cipher import AES
from Crypto import Random
import sys
import uuid

PAN_IV_SALT = 'G\xca\x18\xeefMc\xc5\xb0\x81\x00\xefl\x86\x18@'

PY2 = sys.version_info[0] == 2
PY3 = sys.version_info[0] == 3

if PY2:
    def to_bytes(s):
        if isinstance(s, str):
            return s
        if isinstance(s, unicode):
            return s.encode('utf-8')

    to_str = to_bytes

    bchr = chr

    bord = ord

elif PY3:
    def to_bytes(s):
        if isinstance(s, bytes):
            return s
        if isinstance(s, str):
            return s.encode('utf-8')

    def to_str(s):
        if isinstance(s, bytes):
            return s.decode('utf-8')
        if isinstance(s, str):
            return s

    def bchr(s):
        return bytes([s])

    def bord(s):
        return s

def intToBytes(integer) :
    data = '%x' % integer
    if len(data) % 2 != 0 : data = '0' + data
    return data.decode('hex')

def bytesToInt(bytes) :
    return int(bytes.encode('hex'), 16)

def alignAES(data) :
    data = to_bytes(data)
    rem = AES.block_size - len(data) % AES.block_size
    return data + Random.new().read(rem-1) + bchr(rem)

def dealignAES(data) :
    return data[:-bord(data[-1])]

def encryptPan(pan) :
    cypher = uuid.uuid4()
    data = alignAES(intToBytes(int(pan)))

    return str(cypher), AES.new(cypher.bytes, AES.MODE_CBC, PAN_IV_SALT).encrypt(data)

def decryptPan(cypher, code) :
    return bytesToInt(dealignAES(AES.new(uuid.UUID(str(cypher)).bytes, AES.MODE_CBC, PAN_IV_SALT).decrypt(str(code))))

def sxor(s1,s2):
    return ''.join(bchr(bord(a) ^ bord(b)) for a,b in zip(s1,s2))

def ssum(s) :
    return bchr(sum(bord(x) for x in list(s)) % 256)

def intTo10Bytes(pan) :
    data = intToBytes(int(pan))
    return bchr(0) * (10 - len(data)) + data

def encryptPan10(pan) :
    cypher = Random.new().read(11)

    data = intTo10Bytes(int(pan))
    if len(data)>10 : raise Exception("Pan too big")
    data += ssum(data)

    return cypher.encode('hex'), sxor(cypher,data)

def decryptPan10(cypher, code) :
    result = sxor(cypher.decode('hex'),str(code))
    if ssum(result[:-1]) != result[-1:] : return None
    return bytesToInt(result[:-1])