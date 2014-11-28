import flask
import mysql.connector
import flaskInit

app = flaskInit.app

sqlConfig = {}
sqlConfig['MYSQL_DATABASE_USER'] = 'feeasyapp'
sqlConfig['MYSQL_DATABASE_PASSWORD'] = 'feeasy12930846'
sqlConfig['MYSQL_DATABASE_DB'] = 'feeasy'
sqlConfig['MYSQL_DATABASE_HOST'] = 'localhost'

sqlConnection = None

def getCursor() :
    #conn = flask.g.cnx_pool.get_connection()
    #return conn.cursor()
    return flask.g.cnx.cursor()

@app.before_first_request
def before_first_request():
    init()
    setup()

@app.before_request
def initContext():
    flask.g.cnx = sqlConnection

def init() :
    global sqlConnection
    sqlConnection = mysql.connector.connect(
         pool_size=10,
         autocommit=True,
         user     =sqlConfig['MYSQL_DATABASE_USER'],
         password =sqlConfig['MYSQL_DATABASE_PASSWORD'],
         host     =sqlConfig['MYSQL_DATABASE_HOST'],
         database =sqlConfig['MYSQL_DATABASE_DB'])

    initContext()


def setup() :
    tables = {
        'verifications' : """
            (token CHAR(32) UNIQUE NOT NULL PRIMARY KEY,
             url TEXT,
             postdata TEXT,
             date DATETIME,
             termUrl VARCHAR(255),
             cookies TEXT,
             apiclass VARCHAR(255),
             INDEX(date)
             )""",
        'receivertokens' : """
            (token CHAR(32) UNIQUE NOT NULL PRIMARY KEY,
             id    INT NOT NULL AUTO_INCREMENT,
             data  BLOB,
             INDEX(id)
             )""",
        }
    for table in tables :
        if tableExists(table) : continue

        getCursor().execute('CREATE TABLE `%s` %s' % (table, tables[table]))


def tableExists(tableName) :
    cursor = getCursor()
    cursor.execute("SHOW TABLES LIKE '%s'" % tableName)
    return cursor.fetchone() is not None

