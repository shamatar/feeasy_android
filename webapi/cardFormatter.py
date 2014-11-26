import re

class CardType :
    allTypes = []
    
    def __init__(self, name, isError, maxCVVLength, validationRegExp, strRanges) :
        self.name = name
        self.isError = isError
        
        self.validationPattern = re.compile(validationRegExp)
        
        self.strRanges = strRanges
        
        CardType.allTypes.append(self)
        
    @staticmethod
    def getByPrefix(prefix) :
        prefix = str(prefix)
        
        for x in CardType.allTypes : 
            if x.hasPrefix(prefix) : return x
            
        return CardType.UNKNOWN_CARD
        
    def hasPrefix(self, prefix) :
        return len([x for x in self.strRanges if x[0]<=prefix<x[1]]) > 0;

class CardNumber :
    def __init__(self, number) :
        self.number = int(number)
        
    def getType(self) :
        return CardType.getByPrefix(self.number)
        
    def prettify(self) :
        number = str(self.number)
        if( len(number)<9 ) : return number
        return '%s **** %s' % (number[0:4], number[-4:])
        
CardType.VISA = CardType("Visa", False, 3, "^4\\d{15}$", [("4", "5")])
CardType.MASTERCARD = CardType("Mastercard", False, 3, "^5[1-5]\\d{14}$", [("51", "56")])
CardType.AMERICAN_EXPRESS = CardType("American Express", False, 4, "^3[47]\\d{13}$", [("34", "38")])
CardType.DISCOVER = CardType("Discover", False, 3, "^6(?:011\\d\\d|5\\d{4}|4[4-9]\\d{3}|22(?:1(?:2[6-9]|[3-9]\\d)|[2-8]\\d\\d|9(?:[01]\\d|2[0-5])))\\d{10}$"
            , [("6011", "6012"), ("622126", "622127"), ("622925", "622926"), ("644","66")])
CardType.JCB = CardType("JCB", False, 3, "^35(?:2[89]|[3-8]\\d)\\d{12}$", [("3528","359")])
CardType.DINERS_CLUB = CardType("Diners Club", False, 3, "^$3(?:0[0-5]\\d|095|[689]\\d\\d)\\d{12}"
            , [("300","306"), ("309", "31"), ("36", "37"), ("38","4")])
CardType.UNKNOWN_CARD = CardType("Unknown", False, 3, "", [])
CardType.MAESTRO = CardType("Maestro", False, 3, "^(?:5[0678]\\d\\d|6304|6390|67\\d\\d)\\d{8,15}$"
            , [("50", "51"), ("56", "59"), ("6304", "6305"), ("6390", "6391"), ("67", "68")])

CardType.ERROR_CARD = CardType("Error", True, 3, "", [])