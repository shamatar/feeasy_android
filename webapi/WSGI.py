import mainapi
import flaskInit

app = flaskInit.app
app.debug=True
application = app

if __name__ == "__main__":
    app.run()
