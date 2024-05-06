package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import westwood222.cloud_alloc.dto.SearchRequest;
import westwood222.cloud_alloc.dto.SearchResponse;
import westwood222.cloud_alloc.service.GoogleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private static final Logger log = LoggerFactory.getLogger(CloudController.class);
    private final GoogleService service;

    @GetMapping
    ResponseEntity<SearchResponse> all(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam Map<String, String> other     // included page and size (stupid spring)
    ) {
        // work around solution
        other.remove("page");
        other.remove("size");

        SearchRequest request = SearchRequest.builder().page(page).size(size).conditions(other).build();
        SearchResponse response;
        try {
            response = service.search(request);
        } catch (IOException e) {
            log.error("Unable to search {}", e.toString());
            return null;
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("{fileId}")
    RedirectView one(@PathVariable("fileId") String fileId) {
        try {
            String viewLink = service.get(fileId);
            return new RedirectView(viewLink);
        } catch (IOException e) {
            log.error("Unable to get file {}: {}", fileId, e.toString());
            return null;
        }
    }
}
