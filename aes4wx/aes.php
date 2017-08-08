<?php
header("content-type:text/html; charset=utf8");
// http://blog.csdn.net/hooloo/article/details/2857842
/*
https://leo108.com/pid-1794.asp
http://www.cnblogs.com/zhongyuan/p/5642035.html
http://blog.csdn.net/qq_16371729/article/details/50015481
1.Java中AES加密与解密默认使用AES/ECB/PKCS5Padding模式；
2.php中的AES算法实现使用AES/ECB/NoPadding

要注意特定的Padding实现跟算法的blockSize有关,这里php的blocksize是16。在php的aes加密前先对源字符串进行Padding，问题得到解决。

--------------------------------------------------------------------------------------------------

PHP的AES128位由mcrypt模块提供，称为MCRYPT_RIJNDAEL_128。
JAVA的AES默认就是128位的。
加密模式有好几种，不同的语言不同的库支持的情况不同。这里选择的是安全且通用的CBC模式。
至于padding，这是最头疼的问题，因为PHP的padding与Java的padding不一样。如果使用NoPadding，则默认又用不了 CBC模式。所以，最好的解决方法是自己padding——在原文末尾加上若干个空格，使原文凑齐16的倍数的长度。
，只有强制原文末尾加上一个换行。**这样每次解密后，将最右边的换行以及其右边的空格裁剪掉**，就得到原文了。
另外，为了兼容，在加密和解密时，需要将内容转换成16进制的字符数组。这样一来，即使加密
解密的内容不是普通文本，而是二进制数据，也可以轻松传送啦。*/

class AesDe{  
    const SHA1 = 'SHA1';
    const CBC = 'cbc';
    const CIPHER = 'rijndael-128';
    const BLOCKSIZE = 128;
    const HASH_ITERATIONS = 10000;
    const KEY_LENGTH = 16;
    //偏移变量
    private $arrIv = array(0xA, 1, 0xB, 5, 4, 0xF, 7, 9, 0x17, 4, 1, 6, 8, 0xC, 0xD, 91);
    //salt 值
    private $arrSalt = array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF); 


    public function aes($ostr, $aes_key, $type='encryptHex'){
        if($ostr==''){
            return '';
        }
        //只能用128解密，虽然大地的demo是256
        $td   = mcrypt_module_open(self::CIPHER, '', self::CBC, '');
        //不能用随机的iv
        $iv   = self::genIV();
        $salt = self::genSalt();
        $key  = self::pbkdf2(self::SHA1, $aes_key, $salt, self::HASH_ITERATIONS, self::KEY_LENGTH);
        mcrypt_generic_init($td, $key, $iv);

        $str = '';
        switch($type){
            case 'encryptHex':
                $str = trim(strtoupper(bin2hex(mcrypt_generic($td, $this->pkcs5Pad($ostr)))));
                break;


            case 'decryptHex':
                $str = trim(mdecrypt_generic($td, hex2bin(strtolower($ostr))));
                break;

            case 'encryptBase64':
                $str = trim(base64_encode(mcrypt_generic($td, $this->pkcs5Pad($ostr))));
                break;


            case 'decryptBase64':
                $str = trim(mdecrypt_generic($td, base64_decode($ostr)));    
        }

        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);

        return $str;
    }


    private function pbkdf2($algorithm, $password, $salt, $count, $key_length) {
        $algorithm = strtolower($algorithm);
        if(!in_array($algorithm, hash_algos(), true)){
            trigger_error('PBKDF2 ERROR: Invalid hash algorithm.', E_USER_ERROR);
        }
        if($count <= 0 || $key_length <= 0) {
            trigger_error('PBKDF2 ERROR: Invalid parameters.', E_USER_ERROR);
        }
        $hash_length = strlen(hash($algorithm, "", true));
        $block_count = ceil($key_length / $hash_length);

        $output = "";
        for($i = 1; $i <= $block_count; $i++) {
            $last = $salt . pack("N", $i);
            $last = $xorsum = hash_hmac($algorithm, $last, $password, true);
            for ($j = 1; $j < $count; $j++) {
                $xorsum ^= ($last = hash_hmac($algorithm, $last, $password, true));
            }
            $output .= $xorsum;
        }
        return substr($output, 0, $key_length);
    }

    private function  genIV() {
        $iv = '';
        foreach($this->arrIv as $value) {
            $iv .= chr($value);
        }
        return $iv;
    }

    private function genSalt() {
        $salt = '';
        foreach($this->arrSalt as $value) {
            $salt .= chr($value);
        }
        return $salt;
    }


    private function pkcs5Pad($data) {
        $blockSize = mcrypt_get_block_size(self::CIPHER, self::CBC);   
        $pad = $blockSize - (strlen($data) % $blockSize);
        return $data.str_repeat(chr($pad), $pad);
    }

}


#$str = '{"sourceype":0,"phone":"1000000004","time":1467449322258,"omengUserId":"146020780028780217","token":"B9B21901F531178E0B72F0DF73BF7343","clientId":"3265260DD98FE8698731221C0F70E921","userSourceId":"146020780028780217"}';
$str = '中国{"sourcl中国f中国z';


$exceptData = 'FAE4C592B5C1754285889AE73D2658AF97709715DEF46481E56370442D921DCD8D3AF909499DA3E01601526EC5753B4398B9EEDE87639BEAC90B9EA8A2076F5CAD37C6834A933F79295DDC18B7724BCE396D65EDE13490D10186D2DDB1B44A7461A26CDB1CF1AFB03C13023D06046E84266392173AB413157D8F854B2AAC7E0420EF07FC4AC067AF184CFC58DD4707B51363AC44A572458B5ABF63A2AFDFA2CB831F154E80FFE0877786A22EF0770CDE7E542266580014AC998F3175C72E5F06B3F5793332F1E07F1EB73C4AC47FF1EACD12FCD3C820ECBD7A14AEE6EB86DA41';

$key = "80DBC293132DF7F75BB3BA2563236559";

$aes = new AesDe();
$encryptData = $aes->aes($str,$key,'encryptBase64');
$data = $aes->aes($exceptData,$key,'decryptHex');
$decryptPHP = $aes->aes($encryptData,$key,'decryptBase64');

echo '<br>source:<br>'.$str.'<br>';
echo '<br>encrypt:<br>'.$encryptData.'<br>';

echo '<br>decrypt:<br>'.$data.'<br>';
echo '<br>decrypt:<br>'.$decryptPHP.'<br>';

?>
