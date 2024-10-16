package pe.edu.cibertec.patitas_frontend_wc.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.patitas_frontend_wc.client.LogoutClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loginfeign")
@CrossOrigin(origins = "http://localhost:5173")
public class LogoutFeignClientController {

    @Autowired
    LogoutClient logoutClient;

    @PostMapping("/logout-async")
    public Mono<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO) {

        System.out.println("Consumiendo con Feign Client");
        LogoutResponseDTO response;


        //validar campos de entrada
        if (logoutRequestDTO.tipoDocumento() == null || logoutRequestDTO.tipoDocumento().trim().length() == 0
                || logoutRequestDTO.numeroDocumento() == null || logoutRequestDTO.numeroDocumento().trim().length() == 0) {

            LoginModel loginModel = new LoginModel("02", "Error: Debe completar correctamente sus credenciales", "");
            response = new LogoutResponseDTO(false, null, "Error: Debe completar correctamente sus credenciales");
        } else {


            try {
                ResponseEntity<LogoutResponseDTO> responseEntity = logoutClient.logout(logoutRequestDTO);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    LogoutResponseDTO logoutResponseDTO = responseEntity.getBody();
                    if (logoutResponseDTO.resultado().equals(true)) {
                        response =  new LogoutResponseDTO(true, logoutResponseDTO.fecha(), logoutResponseDTO.msjError());
                    } else {
                        response = new LogoutResponseDTO(false, null, "Error: No se pudo cerrar sesion");
                    }
                } else {
                    response = new LogoutResponseDTO(false, null, "Error: No se pudo cerrar sesion");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                response = new LogoutResponseDTO(false, null, "Error: Error en el logout");
            }
        }
        return Mono.just(response);
    }

}