package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.practice.AttemptRequest;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.Profile;
import dev.designdeck.api.entity.Question;
import dev.designdeck.api.entity.UserCardState;
import dev.designdeck.api.mapper.PracticeMapper;
import dev.designdeck.api.repository.AppUserRepository;
import dev.designdeck.api.repository.AttemptRepository;
import dev.designdeck.api.repository.ProfileRepository;
import dev.designdeck.api.repository.QuestionRepository;
import dev.designdeck.api.repository.UserCardStateRepository;
import dev.designdeck.api.service.CatalogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SrsAlgorithmTest {

    private PracticeServiceImpl practiceService;

    @Mock private UserCardStateRepository userCardStateRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AttemptRepository attemptRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private CatalogService catalogService;
    @Mock private PracticeMapper practiceMapper;
    @Mock private ObjectMapper mapper;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @Captor private ArgumentCaptor<UserCardState> stateCaptor;

    private AppUser user;
    private Question question;
    private Profile profile;

    @BeforeEach
    void setUp() {
        practiceService = new PracticeServiceImpl(
                userCardStateRepository, questionRepository, attemptRepository,
                profileRepository, appUserRepository, catalogService,
                practiceMapper, mapper, meterRegistry);

        user = new AppUser();
        user.setId(UUID.randomUUID());

        question = new Question();
        question.setId(UUID.randomUUID());

        profile = new Profile();
        profile.setId(user.getId());
        profile.setStreakCount(0);
        profile.setLastActiveDate(LocalDate.now().minusDays(2));

        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(profileRepository.findById(user.getId())).thenReturn(Optional.of(profile));
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);
    }

    @Test
    void correctAnswer_firstTime_intervalBecomesOne() {
        when(userCardStateRepository.findByUser_IdAndQuestion_Id(user.getId(), question.getId()))
                .thenReturn(Optional.empty());

        AttemptRequest request = new AttemptRequest(question.getId(), "got", null, "answer", null);
        practiceService.submit(user.getId(), request);

        verify(userCardStateRepository).save(stateCaptor.capture());
        UserCardState state = stateCaptor.getValue();
        
        assertThat(state.getIntervalDays()).isEqualTo(1);
        assertThat(state.getEase()).isEqualTo(2.6);
        assertThat(state.getTimesSeen()).isEqualTo(1);
        assertThat(state.getTimesCorrect()).isEqualTo(1);
    }

    @Test
    void correctAnswer_afterInterval7_intervalBecomesNext() {
        UserCardState existingState = new UserCardState(user, question);
        existingState.setIntervalDays(7);
        existingState.setEase(2.5);
        existingState.setTimesSeen(3);
        existingState.setTimesCorrect(3);

        when(userCardStateRepository.findByUser_IdAndQuestion_Id(user.getId(), question.getId()))
                .thenReturn(Optional.of(existingState));

        AttemptRequest request = new AttemptRequest(question.getId(), "got", null, "answer", null);
        practiceService.submit(user.getId(), request);

        verify(userCardStateRepository).save(stateCaptor.capture());
        UserCardState state = stateCaptor.getValue();

        assertThat(state.getIntervalDays()).isEqualTo(18); // 7 * 2.5 = 17.5 round to 18
        assertThat(state.getEase()).isEqualTo(2.6);
    }

    @Test
    void incorrectAnswer_resetsIntervalToOne() {
        UserCardState existingState = new UserCardState(user, question);
        existingState.setIntervalDays(10);
        existingState.setEase(2.5);

        when(userCardStateRepository.findByUser_IdAndQuestion_Id(user.getId(), question.getId()))
                .thenReturn(Optional.of(existingState));

        AttemptRequest request = new AttemptRequest(question.getId(), "missed", null, "answer", null);
        practiceService.submit(user.getId(), request);

        verify(userCardStateRepository).save(stateCaptor.capture());
        UserCardState state = stateCaptor.getValue();

        assertThat(state.getIntervalDays()).isEqualTo(1);
        assertThat(state.getEase()).isEqualTo(2.3);
    }
}
