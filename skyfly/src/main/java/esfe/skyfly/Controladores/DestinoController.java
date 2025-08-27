package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/destinos")
public class DestinoController {

    private final IDestinoService destinoService;

    @Autowired
    public DestinoController(IDestinoService destinoService) {
        this.destinoService = destinoService;
    }

    // ------------------ INDEX (ADMIN/AGENTE con paginación) ------------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = Math.max(0, page.orElse(1) - 1);
        int pageSize    = Math.max(1, size.orElse(5));

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Destino> destinosPage = destinoService.buscarTodos(pageable);

        model.addAttribute("destinos", destinosPage);

        int totalPages = destinosPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "destino/index"; // Asegúrate de tener templates/destino/index.html
    }

    // ------------------ ALIAS MANT (ADMIN/AGENTE) ------------------
    @GetMapping("/mant")
    public String destinosMant(Model model,
                               @RequestParam(value = "page") Optional<Integer> page,
                               @RequestParam(value = "size") Optional<Integer> size) {
        // Reutiliza el listado con paginación (así no duplicas lógica)
        return index(model, page, size);
    }

    // ------------------ CLIENTE: solo ver/seleccionar ------------------
    @GetMapping("/index-cliente")
    public String destinosCliente(Model model) {
        List<Destino> destinos = destinoService.buscarTodos(); // listado completo para clientes
        model.addAttribute("destinos", destinos);
        return "cliente/destinos/index"; // templates/cliente/destinos/index.html
    }

    @GetMapping("/cliente")
    public String indexCliente(Model model) {
        return destinosCliente(model);
    }

    // ------------------ CREAR ------------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("destino", new Destino());
        model.addAttribute("action", "create");
        return "destino/mant"; // templates/destino/mant.html
    }

    // ------------------ EDITAR ------------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "edit");
        return "destino/mant";
    }

    // ------------------ VER ------------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "view");
        return "destino/mant";
    }

    // ------------------ ELIMINAR (confirmación) ------------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "delete");
        return "destino/mant";
    }

    // ------------------ PROCESAR CREAR ------------------
    @PostMapping("/create")
    public String saveNuevo(@Valid @ModelAttribute Destino destino,
                            BindingResult result,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            @RequestParam(value = "imageBase64", required = false) String imageBase64,
                            Model model,
                            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("action", "create");
            model.addAttribute("imageBase64", imageBase64); // conservar preview
            return "destino/mant";
        }

        try {
            if (file != null && !file.isEmpty()) {
                destino.setImagen(file.getBytes());
            } else if (imageBase64 != null && !imageBase64.isBlank()) {
                destino.setImagen(decodeBase64Image(imageBase64));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al subir la imagen");
            model.addAttribute("action", "create");
            model.addAttribute("imageBase64", imageBase64);
            return "destino/mant";
        }

        destinoService.crearOeditar(destino);
        redirect.addFlashAttribute("msg", "Destino creado correctamente");
        return "redirect:/destinos";
    }

    // ------------------ PROCESAR EDITAR ------------------
    @PostMapping("/edit")
    public String saveEditado(@Valid @ModelAttribute Destino destino,
                              BindingResult result,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "imageBase64", required = false) String imageBase64,
                              Model model,
                              RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            model.addAttribute("imageBase64", imageBase64);
            return "destino/mant";
        }

        try {
            if (file != null && !file.isEmpty()) {
                destino.setImagen(file.getBytes());
            } else if (imageBase64 != null && !imageBase64.isBlank()) {
                destino.setImagen(decodeBase64Image(imageBase64));
            } else if (destino.getDestinoId() != null) {
                Destino actual = destinoService.buscarPorId(destino.getDestinoId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID de destino inválido: " + destino.getDestinoId()));
                destino.setImagen(actual.getImagen());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la imagen");
            model.addAttribute("action", "edit");
            model.addAttribute("imageBase64", imageBase64);
            return "destino/mant";
        }

        destinoService.crearOeditar(destino);
        redirect.addFlashAttribute("msg", "Destino actualizado correctamente");
        return "redirect:/destinos";
    }

    // ------------------ PROCESAR ELIMINAR ------------------
    @PostMapping("/delete")
    public String deleteDestino(@ModelAttribute Destino destino, RedirectAttributes redirect) {
        if (destino.getDestinoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de destino requerido");
        }
        destinoService.eliminarPorId(destino.getDestinoId());
        redirect.addFlashAttribute("msg", "Destino eliminado correctamente");
        return "redirect:/destinos";
    }

    // ------------------ SERVIR IMAGEN ------------------
    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> getImagen(@PathVariable Integer id) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID de destino inválido: " + id));

        byte[] data = destino.getImagen();
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();

        MediaType type = sniffImageType(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"imagen_" + id + "\"")
                .contentType(type)
                .body(data);
    }

    // ------------------ Util ------------------
    private byte[] decodeBase64Image(String base64) {
        if (base64 == null) return null;
        int comma = base64.indexOf(',');
        if (comma != -1) base64 = base64.substring(comma + 1);
        return Base64.getDecoder().decode(base64);
    }

    private MediaType sniffImageType(byte[] bytes) {
        if (bytes.length > 3 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8) return MediaType.IMAGE_JPEG; // JPG
        if (bytes.length > 4 && bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return MediaType.IMAGE_PNG; // PNG
        if (bytes.length > 3 && bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) return MediaType.IMAGE_GIF; // GIF
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}