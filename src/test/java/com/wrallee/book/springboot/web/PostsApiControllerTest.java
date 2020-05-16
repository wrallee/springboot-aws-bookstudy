package com.wrallee.book.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrallee.book.springboot.domain.posts.Posts;
import com.wrallee.book.springboot.domain.posts.PostsRepository;
import com.wrallee.book.springboot.web.dto.PostsResponseDto;
import com.wrallee.book.springboot.web.dto.PostsSaveRequestDto;
import com.wrallee.book.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void postsSave() throws Exception {
        // given
        String title = "title";
        String content = "content";
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author("author")
                .build();

        String url = "http://localhost:" + port + "/api/v1/posts";

        // when
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void postsUpdate() throws Exception {
        // given
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());

        Long updateId = savedPosts.getId();
        String expectedTitle = "updated title";
        String expectedContent = "updated content";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();

        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        // when
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);

    }

    @Test
    @WithMockUser(roles = "USER")
    public void postsFindById() throws Exception {
        // given
        Posts posts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());

        Long generatedId = posts.getId();

        String url = "http://localhost:" + port + "/api/v1/posts/" + generatedId;

        // when
        MvcResult result = mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(generatedId)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        ObjectMapper mapper = new ObjectMapper();
        PostsResponseDto dto = mapper.readValue(result.getResponse().getContentAsString(), PostsResponseDto.class);

        assertThat(dto.getId()).isEqualTo(generatedId);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void postsDelete() throws Exception {
        // given
        Posts posts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());

        Long generatedId = posts.getId();
        String url = "http://localhost:" + port + "/api/v1/posts/" + generatedId;

        // when
        mvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(generatedId)))
                .andExpect(status().isOk());

        // then
        assertThat(postsRepository.findById(generatedId)).isEmpty();
    }

}
