��������
(API ���� �� ������, ������ �������� ����� �� ������, � ��� ���� �� ����������)
��������� ���������� ����� ����� ��� API ������ ����� SSL (!)

���������� MySQL. �������� ���� feeasy � ������������ feeasyapp � ������� feeasy12930846 ������� ����� ������ � ��� ���� ������, �� ������ � localhost, ��� �������� ��������:

mysql> GRANT ALL ON feeasy.* TO 'feeasyapp'@'localhost' IDENTIFIED BY 'feeasy12930846'; FLUSH PRIVILEGES;

�� ������� ������ MySQL ������. ������ root ������������ ��������� � ������� root �������.
������ ��� ���������� ������� MySQL Python Connector. DEB ����� ����������� � ����� MySQL � ���������������.
����� ��������� flask. ���������: pip install flask

������

(������)
http://37.252.124.233:5000/payapi?recipient_card=676280388571625380&sender_card=676280388571625380&recipient_card_format=pan&sender_card_format=pan&sender_exp_year=16&sender_exp_month=1&sender_csc=555&sum=10000&method=transfer

����
recipient_card - �������
sender_card - �������
recipient_card_format - ������ ���� 'pan', � ���������� ��� ����������� ����� ��������������
sender_card_format - 'pan'
sender_exp_year - ��� (2 �����)
sender_exp_month - ����� (����� ������)
sender_csc - CSC (�����)
sum - ����� � ��������
method - 'transfer'

�����
����� ������� ��� JSON

(������)
{
"error": false, 
"token": "fda1073f83e34dc3b3da2b38c37e91fb", 
"url": "http://37.252.124.233:5000/verification?token=fda1073f83e34dc3b3da2b38c37e91fb"
}

���� error - false - ���������� � �������� ��������� � url �����.
����� ����� ������ - 30 ���