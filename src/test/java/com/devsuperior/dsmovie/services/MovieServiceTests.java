package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;

    private String title;
    private MovieEntity movieEntity;
    private MovieDTO movieDTO;
    private PageImpl<MovieEntity> page;
    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;

    @BeforeEach()
    void setUp() throws Exception {
        title = "Test Movie";
        movieEntity = MovieFactory.createMovieEntity();
        movieDTO = MovieFactory.createMovieDTO();
        page = new PageImpl<>(List.of(movieEntity));
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {

        Pageable pageRequest = PageRequest.of(0, 12);
        when(movieRepository.searchByTitle(title, pageRequest)).thenReturn(page);

        Page<MovieDTO> result = movieService.findAll(title, pageRequest);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getSize());
        assertEquals(result.iterator().next().getTitle(), title);
    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {

        when(movieRepository.findById(existingId)).thenReturn(Optional.of(movieEntity));

        MovieDTO result = movieService.findById(existingId);

        assertNotNull(result);
        assertEquals(result.getId(), existingId);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            movieService.findById(nonExistingId);
        });
    }


    @Test
    public void insertShouldReturnMovieDTO() {
        when(movieRepository.save(any())).thenReturn(movieEntity);

        MovieDTO result = movieService.insert(movieDTO);

        assertNotNull(result);
    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {
        when(movieRepository.getReferenceById(existingId)).thenReturn(movieEntity);
        when(movieRepository.save(any())).thenReturn(movieEntity);

        MovieDTO result = movieService.update(existingId, movieDTO);

        assertNotNull(result);
        assertEquals(result.getId(), existingId);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(movieRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> {
            movieService.update(nonExistingId, movieDTO);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        doNothing().when(movieRepository).deleteById(existingId);
        when(movieRepository.existsById(existingId)).thenReturn(true);

        assertDoesNotThrow(() -> {
            movieService.delete(existingId);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(movieRepository.existsById(existingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            movieService.delete(nonExistingId);
        });
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        when(movieRepository.existsById(dependentId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentId);

        assertThrows(DatabaseException.class, () -> {
            movieService.delete(dependentId);
        });
    }
}
