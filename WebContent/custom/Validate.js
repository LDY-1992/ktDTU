/*
 * 校验
 */

//密码(以字母开头，长度在6~18之间，只能包含字母、数字和下划线)：^[a-zA-Z]\w{5,17}$ 
function isACC(str){
	var pattern = /^[a-zA-Z]\w{5,17}$/;
	return pattern.test(str);
}

//Email地址：^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$
function isEmail(str){
	var pattern = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
	return pattern.test(str);
}

//中文、英文、数字但不包括下划线等符号：^[\u4E00-\u9FA5A-Za-z0-9]+$ 或 ^[\u4E00-\u9FA5A-Za-z0-9]{2,20}$
function isName(str){
	var pattern = /^[\u4E00-\u9FA5A-Za-z0-9]+$/;
	return pattern.test(str);
}

//电话号码正则表达式（支持手机号码，3-4位区号，7-8位直播号码，1－4位分机号）: ((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)
function isAllPhone(str){
	var pattern = /((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/;
	return pattern.test(str);
}

//数字：^[0-9]*$
function isNumber(str){
	var pattern = /^[0-9]*$/;
	return pattern.test(str);
}

//中文、英文、数字包括下划线横杠：^[\u4E00-\u9FA5A-Za-z0-9_-]+$
function isDeviceName(str){
	var pattern = /^[\u4E00-\u9FA5A-Za-z0-9_-]+$/;
	return pattern.test(str);
}

//英文和数字：^[A-Za-z0-9]+$ 或 ^[A-Za-z0-9]{4,40}$
function isNumberAndE(str){
	var pattern = /^[A-Za-z0-9]+$/;
	return pattern.test(str);
}