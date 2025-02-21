package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService scoreService;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private Long existindMovieId;
    private Long nonExistindMovieId;
    private MovieEntity movieEntity;
    private MovieDTO movieDTO;
    private UserEntity userEntity;
    private ScoreEntity scoreEntity;
    private ScoreDTO scoreDTO;

    @BeforeEach
    void setUp() throws Exception {
        existindMovieId = 1L;
        nonExistindMovieId = 2L;
        scoreEntity = ScoreFactory.createScoreEntity();
        movieEntity = MovieFactory.createMovieEntity();
        movieDTO = MovieFactory.createMovieDTO();
        userEntity = UserFactory.createUserEntity();
        scoreDTO = ScoreFactory.createScoreDTO();
    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {

        when(userService.authenticated()).thenReturn(userEntity);
        when(scoreRepository.saveAndFlush(scoreEntity)).thenReturn(scoreEntity);
        when(movieRepository.findById(existindMovieId)).thenReturn(Optional.of(movieEntity));
        when(movieRepository.save(movieEntity)).thenReturn(movieEntity);

        movieEntity.getScores().add(scoreEntity);
        MovieDTO result = scoreService.saveScore(scoreDTO);

        assertNotNull(result);
        assertEquals(movieEntity.getId(), result.getId());
        assertEquals(movieEntity.getTitle(), result.getTitle());
    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

        when(userService.authenticated()).thenReturn(userEntity);
        when(movieRepository.findById(nonExistindMovieId)).thenReturn(Optional.empty());

        scoreDTO = new ScoreDTO(nonExistindMovieId, 4.5);

        assertThrows(ResourceNotFoundException.class, () -> {
            scoreService.saveScore(scoreDTO);
        });
    }
}
