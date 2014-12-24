try:
    import xml.etree.cElementTree as ElementTree
except ImportError:
    import xml.etree.ElementTree as ElementTree

import cairosvg
import pyqrcode
import StringIO

import os.path

#class Contour :
#    def __init__(self):
#        self.outer = []
#        self.inner = []
#
#    def append(self, x, y, dir) :
#        if dir == 0 : self.outer.append((x,y))
#        else : self.inner.append((x,y))

basefile = os.path.join(os.path.dirname(os.path.realpath(__file__)),'qrbase.svg')

class PrettyQr :
    def __init__(self) :
        self.base = ElementTree.ElementTree(file=basefile)
        self.codeplaceholder = self.base.find(".//{http://www.w3.org/2000/svg}rect[@id='codeplace']/..")
        self.codeplace = self.base.find(".//{http://www.w3.org/2000/svg}rect[@id='codeplace']")

        self.placewidth  = float(self.codeplace.attrib['width'])
        self.placeheight = float(self.codeplace.attrib['height'])
        self.placeoffset = [float(self.codeplace.attrib['x']), float(self.codeplace.attrib['y'])]

        self.codeplaceholder.remove(self.codeplace)

    def svgFile(self, svgFile) :
        qr   = ElementTree.ElementTree(file=svgFile)

        qrFullWidth  = float(qr.getroot().attrib['width'])
        qrFullHeight = float(qr.getroot().attrib['height'])

        firstLine = qr.find(".//{http://www.w3.org/2000/svg}line[@class='pyqrline']")

        qrmargins = [
            -float(firstLine.attrib['x1']),
            -float(firstLine.attrib['y1']) + float(firstLine.attrib['stroke-width']) / 2,
            -float(firstLine.attrib['x1']), 0] #[l,t,r,b]
        qrmargins[3] = qrmargins[0] + qrmargins[2] - qrmargins[1]

        qrwidth = qrFullWidth + qrmargins[0] + qrmargins[2]
        qrheight = qrFullHeight + qrmargins[1] + qrmargins[3]

        scalex, scaley = self.placewidth / qrwidth, self.placeheight / qrheight

        for line in qr.iterfind(".//{http://www.w3.org/2000/svg}line[@class='pyqrline']") :
            ElementTree.SubElement(self.codeplaceholder, line.tag, {
                'x1' : str(self.placeoffset[0] + (float(line.attrib['x1']) + qrmargins[0]) * scalex),
                'y1' : str(self.placeoffset[1] + (float(line.attrib['y1']) + qrmargins[1]) * scaley),

                'x2' : str(self.placeoffset[0] + (float(line.attrib['x2']) + qrmargins[0]) * scalex),
                'y2' : str(self.placeoffset[1] + (float(line.attrib['y2']) + qrmargins[1]) * scaley),
                'stroke': 'black',
                'stroke-width': str(float(line.attrib['stroke-width']) * scaley)
            })

    def qrObj(self, qr):
        matrix = [[x for x in y] for y in qr.code]
        cornerMatrix = [[0 for x in y] for y in qr.code]
        contourMatrix = [[None for x in y] for y in qr.code]

        xsize, ysize = len(matrix[0]), len(matrix)

        def stepMatrix( x0, y0, x, y, stepx, stepy, corner, contour) :
            startx, starty = x+(stepx - stepy - 1)/2, y+(stepy + stepx - 1)/2

            #contour.append(x, y, corner)
            contour.append((x, y))

            fwdx1, fwdy1 = startx, starty
            fwdx2, fwdy2 = fwdx1 + stepy, fwdy1 - stepx

            while True :
                x += stepx
                y += stepy

                if stepy!=0 :
                    cornerMatrix[fwdy1][fwdx1] += 1
                if stepy==-1 :
                    contourMatrix[fwdy1][fwdx1] = contour

                if x==x0 and y==y0 : return

                fwdx1 += stepx; fwdy1 += stepy
                fwdx2, fwdy2 = fwdx1 + stepy, fwdy1 - stepx

                if (not (0 <= fwdx1 < xsize and 0 <= fwdy1 < ysize) or (matrix[fwdy1][fwdx1]==corner) ) :
                    return stepMatrix(x0, y0, x, y, -stepy,  stepx, corner, contour)

                if ((0 <= fwdx2 < xsize and 0 <= fwdy2 < ysize) and (matrix[fwdy2][fwdx2]!=corner) ) :
                    return stepMatrix(x0, y0, x, y, stepy, -stepx, corner, contour)

        xrange = range(xsize)
        corner = 0

        #contourStack = []
        contours = []

        for y in range(ysize) :
            for x in xrange :
                corner = (corner + cornerMatrix[y][x]) % 2
                contour = contourMatrix[y][x]

                #if not contour is None : contourStack.append(contour)

                if cornerMatrix[y][x]!=0 :
                    #if cornerMatrix[y][x]>1 or contour is None :
                    #    contourStack.pop()

                    continue
                if corner == matrix[y][x] : continue

                #if corner==0 :
                #    contour = Contour()
                #    contours.append(contour)
                #else :
                #    contour = contourStack[-1]

                contour = []
                contours.append(contour)

                stepMatrix(x,y,x,y,1,0, corner, contour)
                corner = (corner + cornerMatrix[y][x]) % 2

                #if cornerMatrix[y][x]!=2 : contourStack.append(contour)

        scalex, scaley = self.placewidth / xsize, self.placeheight / ysize
        ox, oy = self.placeoffset[0], self.placeoffset[1]
        path = ' '.join('M %s z' % ' '.join('%s,%s' % (ox + p[0]*scalex,oy + p[1]*scaley) for p in c) for c in contours)

        ElementTree.SubElement(self.codeplaceholder, "{http://www.w3.org/2000/svg}path", {
            'd' : path,
            'style': "fill:#000000;stroke:none;fill-rule:evenodd"
        })

    def writePng(self, outfile):
        output = StringIO.StringIO()
        self.base.write(output)

        fout = open(outfile,'wb')
        cairosvg.svg2png(bytestring=output.getvalue(),write_to=fout) #output.getvalue()
        fout.close()

#qrcode = pyqrcode.create("http://abcdefefef", error='Q', mode='alphanumeric')
#qrcode.svg('tt.svg', scale=8)
#
##codeplaceholder = base.find(".//{http://www.w3.org/2000/svg}g[@id='codeplaceholder']")
#
#qqq = PrettyQr()
#qqq.svgFile('tt.svg')
#qqq.writePng('ooo1.png')
#
#qqq = PrettyQr()
#qqq.qrObj(qrcode)
#qqq.writePng('ooo2.png')