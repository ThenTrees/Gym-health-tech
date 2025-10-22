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

  @Operation(
      summary = "Get All Posts",
      description = "Retrieves a list of all posts in the community.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PostResponse.class))
            }),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping
  public ResponseEntity<APIResponse<java.util.List<PostResponse>>> getAllPosts() {
    log.info("Get All Posts Request");
    java.util.List<PostResponse> responses = postService.getAllPosts();
    return ResponseEntity.ok(APIResponse.success(responses));
  }

  @Operation(
      summary = "Toggle Like on Post",
      description = "Toggles like status for a post by a user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Like toggled successfully",
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
  @PostMapping("/{postId}/like")
  public ResponseEntity<APIResponse<PostResponse>> toggleLike(
      @PathVariable("postId") String postId, @RequestParam String userId) {
    log.info("Toggle Like Request for post: {} by user: {}", postId, userId);
    PostResponse response = postService.toggleLike(postId, userId);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(
      summary = "Toggle Save on Post",
      description = "Toggles save status for a post by a user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Save toggled successfully",
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
  @PostMapping("/{postId}/save")
  public ResponseEntity<APIResponse<PostResponse>> toggleSave(
      @PathVariable("postId") String postId, @RequestParam String userId) {
    log.info("Toggle Save Request for post: {} by user: {}", postId, userId);
    PostResponse response = postService.toggleSave(postId, userId);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(
      summary = "Share Post",
      description = "Increments share count for a post.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post shared successfully",
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
  @PostMapping("/{postId}/share")
  public ResponseEntity<APIResponse<PostResponse>> sharePost(
      @PathVariable("postId") String postId, @RequestParam String userId) {
    log.info("Share Post Request for post: {} by user: {}", postId, userId);
    PostResponse response = postService.sharePost(postId, userId);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(
      summary = "Get shared plan details",
      description = "Get plan details from a shared post for public viewing")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plan details retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping("/plans/{planId}")
  public ResponseEntity<APIResponse<Object>> getSharedPlanDetails(@PathVariable String planId) {
    log.info("Get shared plan details for planId: {}", planId);
    Object planDetails = postService.getSharedPlanDetails(planId);
    return ResponseEntity.ok(APIResponse.success(planDetails));
  }

  @Operation(
      summary = "Apply/Copy a shared workout plan",
      description = "Copy a shared plan to the authenticated user's plans")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plan applied successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @PostMapping("/plans/{planId}/apply")
  public ResponseEntity<APIResponse<Object>> applySharedPlan(
      @PathVariable String planId, @RequestParam String userId) {
    log.info("Apply shared plan {} for user: {}", planId, userId);
    Object result = postService.applySharedPlan(planId, userId);
    return ResponseEntity.ok(APIResponse.success(result));
  }
}
