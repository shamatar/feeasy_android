ОПИСАНИЕ
(API пока не полное, запрос комиссии здесь не описан, я его пока не тестировал)
Разумется необходимо будет юзать это API только через SSL (!)

Использует MySQL. Создаётся база feeasy и пользователь feeasyapp с паролем feeasy12930846 имеющий право писать в эту базу данных, но только с localhost, это делается запросом:

mysql> GRANT ALL ON feeasy.* TO 'feeasyapp'@'localhost' IDENTIFIED BY 'feeasy12930846'; FLUSH PRIVILEGES;

На сервере поднял MySQL сервер. Пароль root пользователя совпадает с паролем root системы.
Скрипт для выполнения требует MySQL Python Connector. DEB пакет скачивается с сайта MySQL и устанавливается.
Также требуется flask. Установка: pip install flask

==ЗАПРОС (комиссия)==

http://37.252.124.233:5000/payapi?recipient_card=tb66e1853e1479e72ae9491e5a36618903d1546&sender_card=676280388571625380&sum=10000&method=check

поля
recipient_card - понятно
sender_card - понятно
sum - сумма в копейках
method - 'check'

==ОТВЕТ (комиссия)==
ответ парсишь как JSON

(пример)
{
  "bank": {
    "api_id": "alfaweb", 
    "id": "alfa", 
    "name-ru": "\u0410\u043b\u044c\u0444\u0430-\u0411\u0430\u043d\u043a", 
    "web-site": "http://alfabank.ru/"
  }, 
  "error": false, 
  "fee": 3000, 
  "fee2": 3000, 
  "message": "\u0427\u0430\u0435\u0432\u044b\u0435 \u0434\u043b\u044f \u0412\u0430\u0441\u0438", 
  "sender_card": "6762 **** 5380", 
  "sender_card_type": "Maestro", 
  "sum": 10000, 
  "sum2": 13000
}

Если error - false, 
fee и sum - это комиссия и сумма в случае, если комиссию оплачивает плательщик
fee2 и sum2 - это комиссия и сумма в случае, если комиссию оплачивает получатель
response["bank"]["api_id"] нужно будет передать в transfer параметром api_id (!)

==ЗАПРОС (узнать данные получателя)==

в check указывается только карта получателя:
http://37.252.124.233:5000/payapi?recipient_card=tb66e1853e1479e72ae9491e5a36618903d1546&method=check

==ОТВЕТ (узнать данные получателя)==
{
  "error": false, 
  "message": "\u0427\u0430\u0435\u0432\u044b\u0435 \u0434\u043b\u044f \u0412\u0430\u0441\u0438"
}

==ЗАПРОС (транзакция)==

(пример)
http://37.252.124.233:5000/payapi?recipient_card=676280388571625380&sender_card=676280388571625380&sender_exp_year=16&sender_exp_month=1&sender_csc=555&sum=10000&api_id=alfaweb&method=transfer

поля
recipient_card - понятно
sender_card - понятно
sender_exp_year - год (2 цифры)
sender_exp_month - месяц (номер месяца)
sender_csc - CSC (число)
sum - сумма в копейках
recipientfee - указать значение, отличное от 'y', если комиссию оплачивает отправитель
method - 'transfer'
api_id - обязательный параметр, полученный в запросе check: response["bank"]["api_id"]

==ОТВЕТ (транзакция)==
ответ парсишь как JSON

(пример)
{
"cyphertoken": "t60102b8544e541f787457ae4a61d50573c682ce1c0204fa28e36",
"error": false, 
"token": "fda1073f83e34dc3b3da2b38c37e91fb", 
"url": "http://37.252.124.233:5000/verification?token=fda1073f83e34dc3b3da2b38c37e91fb"
}

Если error - false - открываешь в браузере указанный в url адрес.
Время жизни ссылки - 30 мин

Значение cyphertoken может использоваться в последующем запросе вместо sender_card.

==ОБРАБОТКА РЕЗУЛЬТАТА==
После отображения браузера нужно дождаться редиректа на страницу вида
.../verification-result?success=<success>&transactionid=<transactionid>
где <success> может принимать значения "true" или "false" и обозначает успех операции, а <transactionid> - идентификатор транзакции

==ЗАПРОС (генерация токена)==
В дальнейшем будет исключён
http://192.168.157.21:5000/tokengen?pan=1111222233334444&e-mail=test@example.com&descr=Чаевые%20для%20Васи

==ОТВЕТ (генерация токена)==
{
  "cypher": "68337925a23ab3f0b2d7", 
  "cyphertoken": "t0a98d0d6e4d74c06b6912d082e6d962d68337925a23ab3f0b2d7", 
  "error": false, 
  "forpan": "1111222233334444", 
  "id": "4687390000000000016"
}

Где pan в запросе - произвольное число от 1 до 24 цифр.

Шифртокен (cyphertoken) необходим для оплаты по этому pan. Вместо него может использоваться комбинация id/cypher.

В случае ошибки возвращается подобный ответ:
{
  "error": true, 
  "reason": "pan must be numeric"
}

==Декодирование URL==
1. привести url к lower-case
2. если протокол http или https, домен "feeasy.me", "feeasy.me.", "www.feeasy.me", "www.feeasy.me."
	а. убедиться, что первый элемент пути - "/a/"
	b. считать второй элемент пути подписью
3. если протокол feeasy
	а. убедиться, что первый элемент пути - "/a/"
	b. считать второй элемент пути подписью
4. заменить в подписи "0" на "="
5. декодировать подпись BASE 32
6. первые 128 байт декодированной подписи закодировать (ENCRYPT) RSA с открытым ключом "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIcRIyXNGGEPjDYKTlr6U2nAVZeFiQi2lBtoULYx9Gkn448H7BrpM74MXASiw6p4exk0+6P6CLZUksgpuMw44F5lCCQ5yA/IDmNGGY7+lpi8o1tMf4ijgxPDV47goBJeA9SA7g1YEE0JBaP8q/uqufQn554JBkjv8ys4iFSUc0vwIDAQAB"
7. отбросить нулевые байты из начала и конца декодированных данных
8. убедиться, что первые 4 байта декодированных данных "FESY"
9. получить токен следующим образом: 5-й байт данных (сразу после "FESY") - это первый символ токена. Байты с 6-го по 25-й кодировать в hex и сделать последующими символами токена
10. оставшиеся байты (с 26-го и до конца) считать сообщением пользователю (в UTF-8)

Пример.
если закодированные данные имеют вид
\0\0\0FESYt\0\xab\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0Водопроводчику Васе\0\0\0\0,
то токен будет t00AB0000000000000000000000000000000000
а сообщение: "Водопроводчику Васе"