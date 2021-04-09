package com.example.OtpVerification.Controller;

import com.example.OtpVerification.Model.OtpSystemModel;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OtpSystemRestController {

    private final Map<String, OtpSystemModel> otp_data = new HashMap<>();
    private final static String ACCOUNT_SID = "AC8e9897bf919f9bfe471c54ebd154c524";
    private final static String AUTH_ID = "341414b789bff489f5a2b24bdd480e7a";

    static {
        Twilio.init(ACCOUNT_SID,AUTH_ID);
    }

    @RequestMapping(value="/mobilenumbers/{mobilenumber}/otp", method = RequestMethod.POST)
    public ResponseEntity<Object> sendOTP(@PathVariable("mobilenumber") String mobilenumber){

        OtpSystemModel otpSystem = new OtpSystemModel();
        otpSystem.setMobileNumber(mobilenumber);
        otpSystem.setOtp(String.valueOf(((int)(Math.random()*(10000-1000)))+1000));
        otpSystem.setExpireTime(System.currentTimeMillis()+60000);
        otp_data.put(mobilenumber,otpSystem);

        String message = "You OTP is "+otpSystem.getOtp()+"\nValid for 1 minute";
        Message.creator(new PhoneNumber("+16477877879"),new PhoneNumber("+18647320668"),message).create();

        return new ResponseEntity<> ("OTP is send successfully", HttpStatus.OK);
    }

    @RequestMapping(value="/mobilenumbers/{mobilenumber}/otp", method = RequestMethod.PUT)
    public ResponseEntity<Object> verifyOTP(@PathVariable("mobilenumber") String mobilenumber, @RequestBody OtpSystemModel requestBodyOtpSystem){

        if(requestBodyOtpSystem.getOtp()==null || requestBodyOtpSystem.getOtp().trim().length()<=0){
            return new ResponseEntity<>("Please provide OTP",HttpStatus.BAD_REQUEST);
        }
        if(otp_data.containsKey(mobilenumber)) {
            OtpSystemModel otpSystem = otp_data.get(mobilenumber);
            if(otpSystem!=null){
                if(otpSystem.getExpireTime()>=System.currentTimeMillis()){
                    if(requestBodyOtpSystem.getOtp().equals(otpSystem.getOtp())){
                        otp_data.remove(mobilenumber);
                        return new ResponseEntity<>("OTP is verified successfully",HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Invalid OTP",HttpStatus.BAD_REQUEST);
                }
                return new ResponseEntity<>("OTP is expired",HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Something went wrong..!!",HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Mobile number not found",HttpStatus.NOT_FOUND);
    }
}
