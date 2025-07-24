package ru.practicum.controller.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class UtilViewController {

    @GetMapping("/notfound")
    public Mono<String> notFound() {
        return Mono.just("util/notfound");
    }

    @GetMapping("/error")
    public Mono<String> error() {
        return Mono.just("util/notfound");
    }
}
