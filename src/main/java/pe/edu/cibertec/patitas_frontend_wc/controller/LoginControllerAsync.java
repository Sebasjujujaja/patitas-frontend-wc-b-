package pe.edu.cibertec.patitas_frontend_wc.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO){
    //Validar campos de entrada
        if(loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0){

            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "","","",""));
        }

        try {
            // Consumir servicio backend de autenticacion
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO("00", "", loginRequestDTO.tipoDocumento(), loginRequestDTO.numeroDocumento(), response.nombreUsuario(), response.correoUsuario()));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticacion fallida", "","","", ""));
                        }
                    });
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrio un problema en la autenticacion","","", "", ""));
        }
    }


    // PARTE DEL EXAMEN T2
    @PostMapping("/logout")
    public Mono<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO){

        if (logoutRequestDTO.tipoDocumento() == null || logoutRequestDTO.tipoDocumento().trim().length() == 0 ||
                logoutRequestDTO.numeroDocumento() == null || logoutRequestDTO.numeroDocumento().trim().length() == 0){

            LoginModel loginModel = new LoginModel("99", "Error: debe completar sus credenciales correctamente","");

            return Mono.just(new LogoutResponseDTO(false, null, "Error: debe completar sus credenciales correctamente"));
        }

        return webClientAutenticacion.post()
                .uri("/logout")
                .body(Mono.just(logoutRequestDTO), LogoutResponseDTO.class)
                .retrieve()
                .bodyToMono(LogoutResponseDTO.class)
                .flatMap(response ->{
                if (response.resultado().equals(true)){
                    return Mono.just(new LogoutResponseDTO(true, response.fecha(), response.msjError()));
                } else
                    return Mono.just(new LogoutResponseDTO(false,null, "Error: no se pudo cerrar sesión"));
            })
                .onErrorResume(e ->{
                    System.out.println(e.getMessage());
                    return Mono.just(new LogoutResponseDTO(false, null,"Error: en logout"));
            });
    }
}
