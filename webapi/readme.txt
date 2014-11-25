ОПИСАНИЕ
(API пока не полное, запрос комиссии здесь не описан, я его пока не тестировал)
Разумется необходимо будет юзать это API только через SSL (!)

Использует MySQL. Создаётся база feeasy и пользователь feeasyapp с паролем feeasy12930846 имеющий право писать в эту базу данных, но только с localhost, это делается запросом:

mysql> GRANT ALL ON feeasy.* TO 'feeasyapp'@'localhost' IDENTIFIED BY 'feeasy12930846'; FLUSH PRIVILEGES;

На сервере поднял MySQL сервер. Пароль root пользователя совпадает с паролем root системы.
Скрипт для выполнения требует MySQL Python Connector. DEB пакет скачивается с сайта MySQL и устанавливается.
Также требуется flask. Установка: pip install flask

ЗАПРОС

(пример)
http://37.252.124.233:5000/payapi?recipient_card=676280388571625380&sender_card=676280388571625380&recipient_card_format=pan&sender_card_format=pan&sender_exp_year=16&sender_exp_month=1&sender_csc=555&sum=10000&method=transfer

поля
recipient_card - понятно
sender_card - понятно
recipient_card_format - должно быть 'pan', в дальнейшем для токинизации будет использоваться
sender_card_format - 'pan'
sender_exp_year - год (2 цифры)
sender_exp_month - месяц (номер месяца)
sender_csc - CSC (число)
sum - сумма в копейках
method - 'transfer'

ОТВЕТ
ответ парсишь как JSON

(пример)
{
"error": false, 
"token": "fda1073f83e34dc3b3da2b38c37e91fb", 
"url": "http://37.252.124.233:5000/verification?token=fda1073f83e34dc3b3da2b38c37e91fb"
}

Если error - false - открываешь в браузере указанный в url адрес.
Время жизни ссылки - 30 мин