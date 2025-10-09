package org.example.stcapstonebackend.debate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debate")
@RequiredArgsConstructor
public class DebateController {
    private final DebatePostService debatePostService;

    @PostMapping
    public ResponseEntity<DebatePostResponse> createPost(@Valid @RequestBody DebatePostRequest postDto){
        DebatePostResponse responseDto = debatePostService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebatePostResponse> updatePost
            (@Valid @RequestBody DebatePostRequest postDto, @PathVariable Long id) throws Exception{
        DebatePostResponse responseDto = debatePostService.updatePost(postDto,id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id){
        debatePostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebatePostResponse> getPost(@PathVariable Long id) throws Exception{
        DebatePostResponse responseDto = debatePostService.getPost(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .body(responseDto);
    }
}

