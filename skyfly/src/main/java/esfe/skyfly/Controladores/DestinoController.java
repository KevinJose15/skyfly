package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Destino> destinosPage = destinoService.buscarTodos(pageable);

        model.addAttribute("destinos", destinosPage);

        int totalPages = destinosPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // OJO: respetamos tu vista con I mayúscula
        return "destino/Index";
    }

    // ------------------ ALIAS MANT (ADMIN/AGENTE) ------------------
    // Útil si tu SecurityConfig protege /destinos/mant/**
    @GetMapping("/mant")
    public String destinosMant(Model model) {
        // Si prefieres paginar aquí, puedes reutilizar index(...)
        List<Destino> destinos = destinoService.buscarTodos(); // o destinoService.obtenerTodo()
        model.addAttribute("destinos", destinos);
        return "Destinos/mant"; // respetando tu naming existente
    }

    // ------------------ CLIENTE: solo ver/seleccionar ------------------
    // Vista: templates/Cliente/destinos/index.html
    @GetMapping("/index-cliente")
    public String destinosCliente(Model model) {
        List<Destino> destinos = destinoService.buscarTodos(); // listado completo para clientes
        model.addAttribute("destinos", destinos);
        return "Cliente/destinos/index";
    }

    // Alias legible
    @GetMapping("/cliente")
    public String indexCliente(Model model) {
        return destinosCliente(model);
    }

    // ------------------ CREAR ------------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("destino", new Destino());
        model.addAttribute("action", "create");
        return "destino/mant";
    }

    // ------------------ EDITAR ------------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "edit");
        return "destino/mant";
    }

    // ------------------ VER ------------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "view");
        return "destino/mant";
    }

    // ------------------ ELIMINAR (confirmación) ------------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de destino inválido: " + id));
        model.addAttribute("destino", destino);
        model.addAttribute("action", "delete");
        return "destino/mant";
    }

    // ------------------ PROCESAR CREAR ------------------
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Destino destino,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            @RequestParam(value = "imageBase64", required = false) String imageBase64,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("action", "create");
            model.addAttribute("imageBase64", imageBase64); // conservar preview
            return "destino/mant";
        }

        // 1) Si viene archivo, usarlo
        try {
            if (file != null && !file.isEmpty()) {
                destino.setImagen(file.getBytes());
            }
            // 2) Si no hay archivo pero sí base64, usarlo
            else if (imageBase64 != null && !imageBase64.isBlank()) {
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
    public String saveEditado(@ModelAttribute Destino destino,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "imageBase64", required = false) String imageBase64,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            model.addAttribute("imageBase64", imageBase64);
            return "destino/mant";
        }

        try {
            // 1) Si viene archivo nuevo, usarlo
            if (file != null && !file.isEmpty()) {
                destino.setImagen(file.getBytes());
            }
            // 2) Si no hay archivo pero sí base64 (preview), usarlo
            else if (imageBase64 != null && !imageBase64.isBlank()) {
                destino.setImagen(decodeBase64Image(imageBase64));
            }
            // 3) Si no hay archivo ni base64, conservar la imagen actual de BD
            else if (destino.getDestinoId() != null) {
                Destino actual = destinoService.buscarPorId(destino.getDestinoId())
                        .orElseThrow(() -> new IllegalArgumentException("ID de destino inválido: " + destino.getDestinoId()));
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
        destinoService.eliminarPorId(destino.getDestinoId());
        redirect.addFlashAttribute("msg", "Destino eliminado correctamente");
        return "redirect:/destinos";
    }

    // ------------------ SERVIR IMAGEN ------------------
    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> getImagen(@PathVariable Integer id) {
        Destino destino = destinoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de destino inválido: " + id));

        if (destino.getImagen() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"imagen_" + id + ".jpg\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(destino.getImagen());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------ Util ------------------
    private byte[] decodeBase64Image(String base64) {
        if (base64 == null) return null;
        int comma = base64.indexOf(',');
        if (comma != -1) base64 = base64.substring(comma + 1);
        return Base64.getDecoder().decode(base64);
    }
}
