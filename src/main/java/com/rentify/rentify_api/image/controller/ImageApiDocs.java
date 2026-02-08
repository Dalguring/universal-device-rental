package com.rentify.rentify_api.image.controller;

import com.rentify.rentify_api.image.dto.ImageUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image API", description = "이미지 등록 API")
public interface ImageApiDocs {

    @Operation(summary = "이미지 등록", description = "게시글 생성 시 이미지를 등록합니다.<br/>여러 장의 이미지 등록이 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "이미지 등록 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "success": true,
                            "code": "201",
                            "message": "이미지 등록완료",
                            "data": {
                                "imageUrls": [
                                    "http://43.201.87.180:8080/images/189f72a7-ae1a-4eb2-a004-57124ca7e0a9.jpg",
                                    "http://43.201.87.180:8080/images/ffcbfd16-9afe-433d-b30d-3877b1470580.png"
                                ]
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "파일 업로드 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "파일 개수 초과",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "파일은 최대 5개까지만 업로드 가능합니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "파일 용량 초과",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "최대 10MB 까지만 업로드 가능합니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "지원하지 않는 파일 형식",
                        value = """
                            {
                                "success": false,
                                "code": "400",
                                "message": "지원하지 않는 파일 형식입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<com.rentify.rentify_api.common.response.ApiResponse<ImageUploadResponse>> upload(
        @RequestPart("files") List<MultipartFile> files
    );
}
