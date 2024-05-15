package com.example.auth.controller;

import com.example.auth.entity.*;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtils;
import com.example.auth.security.UserDetailsImpl;
import com.google.common.net.HttpHeaders;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Générer le token JWT
        String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Récupérer le rôle de l'utilisateur
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER");

        // Construire et renvoyer la réponse contenant le token et les informations de l'utilisateur
        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getEmail());
        response.put("role", role);
        response.put("token", jwt); // Inclure le token JWT ici

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Créez un nouvel utilisateur avec un seul rôle
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Role role = roleRepository.findByName(signUpRequest.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
    @PostMapping("/role")
    public void addRole(@RequestBody Role role){
        roleRepository.save(role);
    }
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> gettAllRoles(){
        return roleRepository.findAll();
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id){
        return userRepository.findById(id).get();
    }
    @DeleteMapping("/role/{id}")
    public void deleteRole(@PathVariable Long id){
        roleRepository.deleteById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userRepository.deleteById(id);
    }
    @GetMapping("/count")
    public Long countUser(){
        return userRepository.count();
    }
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user){
        User u = userRepository.findById(id).get();
        u.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(u);
    }
    @GetMapping("/user/{username}")
    public User getUserByUsername(@PathVariable String username){
        return userRepository.findByUsername(username).get();
    }
    @GetMapping("/users/admin")
    public List<User> getAllAdminUsers() {
        return userRepository.findByRoleName("ADMIN");
    }
    @PutMapping("/update/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Vérifier si le nouvel email est déjà utilisé par un autre utilisateur
            if (!user.getEmail().equals(updateUserRequest.getEmail()) && userRepository.existsByEmail(updateUserRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
            }

            // Vérifier si le nouvel username est différent de l'existant
            if (!user.getUsername().equals(updateUserRequest.getUsername())) {
                // Vérifier si le nouvel username est déjà utilisé par un autre utilisateur
                if (userRepository.existsByUsername(updateUserRequest.getUsername())) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
                }
                // Mettre à jour le nom d'utilisateur
                user.setUsername(updateUserRequest.getUsername());
            }

            user.setEmail(updateUserRequest.getEmail());

            // Vérifier si un nouveau mot de passe a été fourni
            if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
                user.setPassword(encoder.encode(updateUserRequest.getPassword()));
            }

            userRepository.save(user);
            String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

            // Renvoyer le nouveau token JWT en tant que réponse au frontend
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().getName());
            response.put("token", jwt);

            return ResponseEntity.ok(new MessageResponse("User updated successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchUserByUsername(@RequestParam String username) {
        Optional<User> optionalUser = userRepository.findByUsern(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Traitement avec l'utilisateur trouvé, par exemple retourner l'utilisateur dans la réponse
            return ResponseEntity.ok(user);
        } else {
            // Gérer le cas où aucun utilisateur n'est trouvé avec ce nom d'utilisateur
            return ResponseEntity.notFound().build();
        }
    }



}