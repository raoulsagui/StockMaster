package com.example.backend.security;

import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de UserDetailsService extraite de SecurityConfig.
 *
 * Pourquoi cette classe séparée ?
 * Pour éviter une dépendance circulaire :
 *
 *   JwtAuthenticationFilter → UserDetailsService
 *   SecurityConfig          → JwtAuthenticationFilter
 *   SecurityConfig          → UserDetailsService (bean défini dedans)  ← cycle !
 *
 * En isolant UserDetailsService ici avec @Service, Spring peut
 * l'instancier indépendamment, ce qui brise le cycle.
 *
 * Spring Security appelle loadUserByUsername() pour retrouver
 * l'utilisateur à partir de son email lors de l'authentification.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // On cherche l'utilisateur par email dans la BDD.
        // Notre entité Utilisateur implémente UserDetails,
        // donc on peut la retourner directement.
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Utilisateur non trouvé : " + email
                ));
    }
}
