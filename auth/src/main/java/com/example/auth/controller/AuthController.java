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

        // Récupérer les rôles de l'utilisateur
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Construire et renvoyer la réponse contenant le token et les informations de l'utilisateur
        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getEmail());
        response.put("roles", roles);
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

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName("ADMIN")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "USER":
                        Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
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
    // modfier only mot de passe
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
        return userRepository.findByRolesName("ADMIN");
    }
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserDetails(@RequestBody UpdateUserRequest updateUserRequest) {
        // Récupérer l'identifiant de l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("User details could not be updated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        Long userId = userDetails.getId();

        // Vérifier si l'utilisateur existe dans la base de données
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }
        User user = optionalUser.get();

        // Mettre à jour le nom d'utilisateur et l'e-mail si les valeurs sont fournies dans la requête
        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().isEmpty()) {
            user.setUsername(updateUserRequest.getUsername());
        }
        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
            user.setEmail(updateUserRequest.getEmail());
        }

        // Enregistrer les modifications dans la base de données
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User details updated successfully"));
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