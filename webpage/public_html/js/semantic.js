/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(function(){
    var startColor  = [0xff,0xdd,0x55];
    var endColor    = [0x87,0xaa,0xde];
    var maxViewPortDiff = 80;
    
    var clamp = function(n, min, max) {
        return Math.min(Math.max(n, min), max);
    };
    
    var boxContent = $('.box-content');
    var grad = $('.mainimg-grad div');
    
    var topShowed = false;
    
    window.onscroll = function() {
        var s = $(window).scrollTop(),
                d = $(document).height(),
                c = $(window).height();
        if( d-c<=0 ) {
            var color = [
                Math.round(startColor[0] + endColor[0])/2,
                Math.round(startColor[1] + endColor[1])/2,
                Math.round(startColor[2] + endColor[2])/2
            ];
        } else {
            var scroll1 = (s / (d-c));
            var scroll2 = 1-scroll1;
            
            var cdiff = maxViewPortDiff*d/c;
            var rEndColor=[
                clamp(endColor[0],startColor[0]-cdiff,startColor[0]+cdiff),
                clamp(endColor[1],startColor[1]-cdiff,startColor[1]+cdiff),
                clamp(endColor[2],startColor[2]-cdiff,startColor[2]+cdiff)
            ];
            var color = [
                Math.round(startColor[0]*scroll2 + rEndColor[0]*scroll1),
                Math.round(startColor[1]*scroll2 + rEndColor[1]*scroll1),
                Math.round(startColor[2]*scroll2 + rEndColor[2]*scroll1)
            ];
        }
        
        var showTop = s > c - $('.box-header').height();
        if( topShowed!==showTop ) {
            topShowed = showTop;
            height = $('.box-header').height();
            if( topShowed ) {
                $('.box-header')
                        .show()
                        .css('top',-height)
                        .animate({'top':0}, 150);
                //$('.box-header').fadeIn(100);
            }
            else {
                $('.box-header')
                        .animate({'top':-height}, 150);
                        //fadeOut(100);
            }
        }
        
        colorSpec = 'rgb('+color[0]+','+color[1]+','+color[2]+')';
        boxContent.css('background-color', colorSpec);
        grad.css('box-shadow', '0px 0px 10vh 10vh '+colorSpec);
    };
    
    window.onscroll();
    
    var genshowed = false;
    
    var showGenWindow = function(show) {
        genshowed = show;
        $('.top-menu').toggleClass('selected', genshowed);
        if( genshowed ) $('.codegen').fadeIn(200);
        else $('.codegen').fadeOut(200);
    };
    $('ul.top-menu li').click(function() {
        showGenWindow(!genshowed);
    });
    
    var showHelper = function(formHelper, to, text) {
        formHelper.finish();
        
        formHelper.find('.comment-inner-helper').text(text);
        formHelper.css('top', Math.round($(to).position().top - formHelper.outerHeight()));
        formHelper.fadeIn(200);
    };
    var hideHelper = function(formHelper) {
        formHelper.finish();
        formHelper.fadeOut(200);
    };
    
    var goodHelper = $('#form-helper');
    var badHelper = $('#form-helper-err');
    
    var bindHelper = function(elem, cont) {
        $(elem).focusin(function(){
            hideHelper(badHelper);
            showHelper(goodHelper,elem,cont());
        });
        $(elem).focusout(function(){
            hideHelper(goodHelper);
        });
    };
    
    var bindInputs = function(form, inputs) {
        for(var inputIdx in inputs) {
            var input = inputs[inputIdx];
            bindHelper(input, input.helpFnc);
        }
        form.onsubmit=function(){
            hideHelper(badHelper);
            for(var inputIdx in inputs) {
                var input = inputs[inputIdx];
                input.value = $(input).find('input')[0].value;
                res = input.validateFnc();
                if( res!==true ) {
                    hideHelper(goodHelper);
                    showHelper(badHelper, input, res);
                    break;
                }
            }
            return false;
        };
    };
    
    var holderMail = document.getElementById('holderMail');
    var holderMsg  = document.getElementById('holderMsg');
    var holderCard = document.getElementById('holderCard');
    var genForm = document.getElementById('genform');
    
    holderMail.helpFnc = function(){
        return 'код будет выслан на этот адрес';
    };
    holderMsg.helpFnc = function(){
        return 'Комментарий будет видеть отправитель платежа. Постарайтесь быть лаконичным и точным';
    };
    holderCard.helpFnc = function(){
        return 'номер кредитной карты, на которую вы хотите получать благодарности';
    };
    
    holderMail.validateFnc = function() {
        if( this.value === null || this.value === "") {
            return "введите e-mail";
        }
        var atpos = this.value.indexOf("@");
        if( atpos<1 || atpos>this.value.length - 2 ) {
            return "некорректный адрес";
        }
        return true;
    };
    holderMsg.validateFnc=function() {
        if( this.value === null || this.value === "") {
            return "введите комментарий";
        }
        return true;
    };
    
    var luhnCheck = function(value) {
	var nCheck = 0, nDigit = 0, bEven = false;
        for (var n = value.length - 1; n >= 0; n--) {
            var cDigit = value.charAt(n),
                nDigit = parseInt(cDigit, 10);

            if (bEven) {
                if ((nDigit *= 2) > 9) nDigit -= 9;
            }

            nCheck += nDigit;
            bEven = !bEven;
	}
 
	return (nCheck % 10) == 0;
    };
    
    holderCard.validateFnc=function() {
        if( this.value === null || this.value === "") {
            return "введите номер карты";
        }
        if( /[^0-9-\s]+/.test(this.value) ) {
            return "номер карты должен быть числовым";
        }
        var val = this.value.replace(/\D/g, "");
        if( val.length<9 || val.length>19 || !luhnCheck(val) )
            return "неверный номер карты";
        
        return true;
    };
    
    bindInputs(genForm, [holderMail,holderMsg,holderCard]);
});