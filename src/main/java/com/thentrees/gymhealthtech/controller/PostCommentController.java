package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.service.PostCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.prefix}/comments")
@RequiredArgsConstructor
@Slf4j
public class PostCommentController {

  private final PostCommentService commentService;

  @Operation(
      summary = "Create a new comment on a post",
      description =
          "Creates a new comment for the specified post and returns the created comment information.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment created successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PostCommentResponse.class))
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
  public ResponseEntity<APIResponse<PostCommentResponse>> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    PostCommentResponse response = commentService.createPostComment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(response));
  }

  @Operation(
      summary = "Get comments for a post",
      description = "Retrieves all comments associated with the specified post.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PostCommentResponse.class))
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
  public ResponseEntity<APIResponse<List<PostCommentResponse>>> getCommentsByPost(
      @PathVariable String postId) {
    List<PostCommentResponse> responses = commentService.getPostComments(postId);
    return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(responses));
  }
}
