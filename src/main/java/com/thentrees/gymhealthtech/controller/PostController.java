package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.custom.PremiumOnly;
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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AppConstants.API_V1 + "/posts")
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
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<APIResponse<PostResponse>> createPost(
      @RequestPart("post") CreatePostRequest request,
      @RequestPart(value = "files", required = false) List<MultipartFile> files) {
    PostResponse response = postService.createPost(request, files);
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
    PostResponse response = postService.getPostDetail(postId);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(
      summary = "Delete a Post",
      description = "Deletes a specific post by its ID if the user has the necessary permissions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post deleted successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = String.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission to delete this post",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
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
  @DeleteMapping("/{postId}")
  public ResponseEntity<APIResponse<String>> deletePost(
    @PathVariable("postId") UUID postId, Authentication authentication) {
    postService.deletePost(postId, authentication);
    return ResponseEntity.ok(APIResponse.success(SuccessMessages.DELETE_POST_SUCCESS));
  }

  @Operation(
      summary = "Update a Post",
      description = "Updates the details of a specific post by its ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post updated successfully",
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
  @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<PostResponse>> updatePost(
      @PathVariable("postId") UUID postId,
      @Valid @RequestPart("post") CreatePostRequest request,
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      Authentication authentication) {

    PostResponse response = postService.updatePost(postId, request, files, authentication);
    return ResponseEntity.ok(APIResponse.success(response, "Post updated successfully"));
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
  public ResponseEntity<APIResponse<List<PostResponse>>> getAllPosts() {
    List<PostResponse> responses = postService.getAllPosts();
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
  public ResponseEntity<APIResponse<String>> toggleLike(
      @PathVariable("postId") UUID postId, Authentication authentication) {
    postService.toggleLike(postId, authentication);
    return ResponseEntity.ok(APIResponse.success("Like status toggled successfully"));
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
      @PathVariable("postId") UUID postId) {
    PostResponse response = postService.toggleSave(postId);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @Operation(summary = "Share Post", description = "Increments share count for a post.")
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
      @PathVariable("postId") UUID postId) {
    PostResponse response = postService.sharePost(postId);
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
  public ResponseEntity<APIResponse<Object>> getSharedPlanDetails(@PathVariable UUID planId) {
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
    Object result = postService.applySharedPlan(planId, userId);
    return ResponseEntity.ok(APIResponse.success(result));
  }

  @Operation(
      summary = "Delete Post Media",
      description = "Deletes a specific media file from a post.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Media deleted successfully",
            content = @Content()),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission to delete this media",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Post or media not found",
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

  @DeleteMapping("/{postId}/media")
  public ResponseEntity<Void> deletePostMedia(
      @PathVariable UUID postId,
      @RequestParam("url") String mediaUrl,
      Authentication authentication) {
    postService.deletePostMedia(postId, mediaUrl, authentication);
    return ResponseEntity.noContent().build();
  }
}
