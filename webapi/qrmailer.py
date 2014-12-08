# -*- coding: utf-8 -*-
#ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~

from Crypto.Hash import SHA
from Crypto.PublicKey import RSA

from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
import email.Encoders

import smtplib

import urllib
import base64

import pyqrcode
#import qrcode.image.svg

import os.path
import tempfile
import shutil

MESSAGE_MAX_LEN = 100

def generatePayUrl(message, cyphertoken) :
    privKey = base64.b64decode("""
        MIICXAIBAAKBgQDIcRIyXNGGEPjDYKTlr6U2nAVZeFiQi2lBtoULYx9Gkn448H7B
        rpM74MXASiw6p4exk0+6P6CLZUksgpuMw44F5lCCQ5yA/IDmNGGY7+lpi8o1tMf4
        ijgxPDV47goBJeA9SA7g1YEE0JBaP8q/uqufQn554JBkjv8ys4iFSUc0vwIDAQAB
        AoGADV5VO1yrcMii/szsUpFjcH/5b10ycvmJXKOivf6vcFKG7p9DpQFSupSkO832
        ozbXOwW7NI3X1FQ+DCTt4PaN+0KIk8SgYPHk6gWwgVNQu5NBvn8C+JZFLgKSR36c
        iZbPSQTy+0X4mzFlQDsuaQqek0pTeoeMAwlSdmzXueva3OkCQQDhC2L8CYPiVLF4
        FxLVO7i1uZuWA316lPy7rW9jmgsWKzjGDUlT3QMPXISfz+sPVGfXwcMkrH+3e0kd
        MVwWNFgbAkEA5ANVNWjYTj9rz/9hZIdzzyKYyZUfcmWESTn2ZT6xokBNSz7IPNSb
        ybo2AJRD8kntRp1FdlanF4AtzQixoOaoLQJADWjBSCVE/3pubKprS9tYITPjCmhA
        1MoF2ST3ayblnDeNFXf7M40KvqKToHYiGnK8EUYRW6EPpx4oeB3Vz9jkmwJAdKjL
        4N66xRYn2CzGrMOs2g7Oc1zr0QDltP2F9nkkM2qI4XXGx3DUIFs4pAU32nA+iUGQ
        N6LQLPL0/BF1qGg7JQJBALOVuKCiP50EMWSp6xfWix5dVWVhFGDlrBuTPgW5kC7Q
        Ru43x+BXCDTHYgGcYTFvvfVJCCdI6e3p3/Sz0sc1RqE=""".replace('\t','').replace(' ','').replace('\r','').replace('\n',''))
        
    keyForSigning = RSA.importKey(privKey)
    #query = urllib.urlencode({"d": base64.urlsafe_b64encode(message[128 - 21:]).replace("=","~"), cyphertoken[0]: cyphertoken[1:]})
    #
    #hash = SHA.new()
    #hash.update(query)
    #signature = base64.urlsafe_b64encode(keyForSigning.decrypt(message[0:128 - 21] + hash.digest())).replace("=","~")
    data = "FESY" + cyphertoken[0] + cyphertoken[1:].decode('hex') + message.encode('utf-8')
    cryptedData = keyForSigning.decrypt(data)

    return "https://feeasy.me/a/%s" % base64.b32encode(cryptedData).replace('=','0')
    
def makeQR(link, tmpfolder, fileprefix) :
    svgFileName = os.path.join(tmpfolder, fileprefix+'.svg')
    pngFileName = os.path.join(tmpfolder, fileprefix+'.png')
    
    #qr = qrcode.QRCode(
    #    version=1,
    #    error_correction=qrcode.constants.ERROR_CORRECT_Q,
    #    box_size=10,
    #    border=4,
    #)
    #qr.add_data(link)
    #qr.make(fit=True)
    
    #imgPng = qr.make_image()
    #imgPng.save(pngFileName)
    
    #imgSvg = qr.make_image(image_factory=qrcode.image.svg.SvgPathImage)
    #imgSvg.save(svgFileName)
    
    qr = pyqrcode.create(link, error='Q', mode='alphanumeric')
    qr.svg(svgFileName, scale=8)
    qr.png(pngFileName, scale=8)
    
    return [pngFileName, svgFileName]#, pngFileName]
    
def mailQR(dstmail, files, card, message) :
    # Open a plain text file for reading.  For this example, assume that
    # the text file contains only ASCII characters.
    
    text=u"""
Здравствуйте, это команда Feeasy!

В этом письме находится ваша feeasy-тка, которая позволит Вам получать вознаграждения на указанную карту:
 %(card)s.

Вы также указали назначение платежа, а именно: 
 %(message)s

Распакуйте архив, который приложен к этому письму и в нём вы найдёте ваш персональный QR-код в высоком векторном качестве! Для вашего удобства мы приложили также также и обычное изображение.

Вы можете распечатать визитку с вашим персональным QR-кодом или сделать наклейки, а можете вставить его в презентацию. Теперь каждый сможет отблагодарить Вас не только на словах, но и материально!

Будьте внимательны, для Вашей безопасности мы не храним данные Вашей карты у себя на сервере. 

Спасибо, что вы с нами!
http://feeasy.me""" % {'card': card, 'message': message}

    me = "Feeasy Team <feeasy@xpianotools.com>"
    you = dstmail
    
    msg = MIMEMultipart('mixed')
    msg['From'] = me
    msg['To']   = you
    msg['Subject'] = "Ваша feeasy-тка готова!"
    
    msg.attach(MIMEText(text.encode('utf-8'),'plain','utf-8'))
    
    for file in files :
        fileAttachment = MIMEBase('application', "octet-stream")
        fileAttachment.set_payload(open(file, "rb").read())
        email.Encoders.encode_base64(fileAttachment)
        fileAttachment.add_header('Content-Disposition', 'attachment; filename= "%s"' % os.path.basename(file))
        msg.attach(fileAttachment)

    # Send the message via our own SMTP server, but don't include the
    # envelope header.
    server = smtplib.SMTP_SSL('smtp.yandex.ru', 465)
    server.login('feeasy@xpianotools.com','feeasy12930846')
    server.sendmail(me, [you], msg.as_string())
    server.quit()
    
def processToken(message, cyphertoken, cardType, cardMask, email) :
    url = generatePayUrl(message, cyphertoken)
    tmpFolder = tempfile.mkdtemp()
    files = makeQR(url, tmpFolder, cardType + cardMask[-4:])
    mailQR(email, files, "%s %s" % (cardType, cardMask), message)
    
    shutil.rmtree(tmpFolder)

#processToken("Это сообщение, котороя я написал! Это сообщение!!!", 'tf1f2f3f4f5f6f7f80102030405060708090a0b', 'Maestro', '4444 **** 3333', 'gaosipov@gmail.com')