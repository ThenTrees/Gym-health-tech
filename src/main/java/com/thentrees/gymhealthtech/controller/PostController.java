package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.prefix}/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

  private final PostService postService;

  @Operation(
      summary = "Create a new post",
      description =
          "Creates a new post with the provided details and returns the created post information.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Post created successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PostResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @PostMapping
  public ResponseEntity<APIResponse<PostResponse>> createPost(
      @Valid @RequestBody CreatePostRequest request) {
    log.info("Create Post Request: {}", request);
    PostResponse response = postService.createPost(request);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(
      summary = "Get Post Detail",
      description = "Retrieves detailed information about a specific post by its ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post details retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PostResponse.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping("/{postId}")
  public ResponseEntity<APIResponse<PostResponse>> getPostDetail(
      @PathVariable("postId") String postId) {
    log.info("Get Post Detail Request: {}", postId);
    PostResponse response = postService.getPostDetail(postId);
    return ResponseEntity.ok(APIResponse.success(response));
  }
}
