var keySize = 128;
var iterationCount = 100; //使用CryptoJS 3.0.2版本循环10000次，效率比CryptoJS 3.1.2版本快10倍
var ivHex = "06080C0D5B0A010B05040F0709170301";
var saltHex = "0001020304050607080A0B0C0D0E0F09";

var passPhrase = "B102000007186E4A554F756103D4590F";

var plainText = "3F550FE089A3BB9A701E0CE46E08C800";
var cipherText = "7QZao1BUuc/y/M+vFYD7Qzz/l/O6sYlZaUbYeN5mmHw=";

var start=new Date();  //开始时间

var aesUtil = new AesUtil(keySize, iterationCount);

var encrypt = aesUtil.encrypt(saltHex, ivHex, passPhrase, plainText);
console.log("encrypt spent time "+(new Date().getTime()-start.getTime()))
encrypted=CryptoJS.enc.Base64.parse(encrypt).toString(CryptoJS.enc.Hex);
console.log(encrypted);

start=new Date();
var decrypt = aesUtil.decrypt(saltHex, ivHex, passPhrase, CryptoJS.enc.Hex.parse(encrypted).toString(CryptoJS.enc.Base64));
console.log("decrypt spent time "+(new Date().getTime()-start.getTime()))
console.log(decrypt);


var md5 = CryptoJS.MD5("1464072712858").toString().toUpperCase();
console.log(md5);