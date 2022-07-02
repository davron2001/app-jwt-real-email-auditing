package uz.ages.appjwtrealemailauditing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.ages.appjwtrealemailauditing.entity.User;
import uz.ages.appjwtrealemailauditing.entity.enums.RoleName;
import uz.ages.appjwtrealemailauditing.payload.ApiResponse;
import uz.ages.appjwtrealemailauditing.payload.LoginDto;
import uz.ages.appjwtrealemailauditing.payload.RegisterDto;
import uz.ages.appjwtrealemailauditing.repository.RoleRepository;
import uz.ages.appjwtrealemailauditing.repository.UserRepository;
import uz.ages.appjwtrealemailauditing.security.JwtProvider;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtProvider jwtProvider;

    public ApiResponse register(RegisterDto registerDto) {
        boolean byEmail = userRepository.existsByEmail(registerDto.getEmail());
        if (byEmail) {
            return new ApiResponse("Bunday email allaqachon qo'shilgan.", false);
        }

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_USER)));
        user.setEmailCode(UUID.randomUUID().toString());
        userRepository.save(user);

        sendEmail(user.getEmail(), user.getEmailCode());
        return new ApiResponse("Ajoyib, Emailni tekshiring.", true);
    }

    public Boolean sendEmail(String sendEmail, String emailCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("test@Formgmail.com");
            message.setTo(sendEmail);
            message.setSubject("Accauntni Tasdiqlsh");
            message.setText("<a href  = 'http://localhost:8080/api/auth/verifyEmail?emailCode=" + emailCode + "+&email=" + sendEmail + "'>Tasdiqlang</a>");
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ApiResponse verifyEmail(String emailCode, String email) {
        Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(true);
            user.setEmailCode(null);
            userRepository.save(user);
            return new ApiResponse("Account tasdiqlandi.", true);
        }
        return new ApiResponse("Account allaqachon tasdiqlangan.", false);
    }

    public ApiResponse login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(),
                    loginDto.getPassword()));
            User user = (User) authentication.getPrincipal();
            String token = jwtProvider.generateToken(loginDto.getUsername(), user.getRoles());
            return new ApiResponse("Token", true, token);
        } catch (BadCredentialsException badCredentialsException) {
            return new ApiResponse("Parol yoki password xato.", false);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username + " not found."));
//        Optional<User> optionalUser = userRepository.findByEmail(username);
//        if (optionalUser.isPresent()) {
//            optionalUser.get();
//        }
//        throw new UsernameNotFoundException(username + " topilmadi.");
    }
}
