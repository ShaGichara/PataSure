package ke.patasure.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser account = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Account not found"
                ));

        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRole())
                .disabled(!account.isEnabled())
                .build();
    }
}
