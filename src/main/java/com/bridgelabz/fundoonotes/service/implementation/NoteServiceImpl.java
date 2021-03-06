package com.bridgelabz.fundoonotes.service.implementation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bridgelabz.fundoonotes.exception.AuthorizationException;
import com.bridgelabz.fundoonotes.exception.NoteException;
import com.bridgelabz.fundoonotes.exception.RemainderException;
import com.bridgelabz.fundoonotes.model.Label;
import com.bridgelabz.fundoonotes.model.Note;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.model.dto.NoteDTO;
import com.bridgelabz.fundoonotes.repository.INoteRepository;
import com.bridgelabz.fundoonotes.repository.IUserRepository;
import com.bridgelabz.fundoonotes.service.INoteService;
import com.bridgelabz.fundoonotes.utility.JWTToken;
import com.bridgelabz.fundoonotes.utility.Util;

/**
 * This class implements {@link INoteService} interface which has the
 * unimplemented functionality of creating a note, updating, deleting and all.
 * All operations will be carried out if the user is a valid user.
 * 
 * @author Durgasankar Mishra
 * @created 2020-01-27
 * @version 1.0
 * @updated 2020-01-30
 * @modified -> optimized code for authentication of user and validation of note
 */
@Service
public class NoteServiceImpl implements INoteService {

	@Autowired
	private IUserRepository userRepository;
	@Autowired
	private INoteRepository noteRepository;
	@Autowired
	private JWTToken jwtToken;
//	@Autowired
//	private RedisTemplate<String, Object> redisTemplate;

//	@Autowired
//	private IElasticSearchRepository elasticSearchRepository;

	/**
	 * This function takes authentication token as String input parameter and decode
	 * token an authenticate user after successful authentication it return the
	 * verified user else throw {@link AuthorizationException}
	 * 
	 * @param token as String input parameter
	 * @return {@link User}
	 */
	private User authenticatedUser(String token) {
		User fetchedUser = userRepository.getUser(jwtToken.decodeToken(token));
		if (fetchedUser != null) {
			return fetchedUser;
		}
		throw new AuthorizationException(Util.USER_AUTHORIZATION_EXCEPTION_MESSAGE,
				Util.USER_AUTHENTICATION_EXCEPTION_STATUS);
	}

	/**
	 * This function takes note id as input parameter check for it's existence in
	 * the database if found valid then return it else throw {@link NoteException}
	 * 
	 * @param noteId as Long input parameter
	 * @return {@link Note}
	 */
	private Note verifiedNote(long noteId) {
		Note fetchedNote = noteRepository.getNote(noteId);
		if (fetchedNote != null) {
			return fetchedNote;
		}
		throw new NoteException(Util.NOTE_NOT_FOUND_EXCEPTION_MESSAGE, Util.NOTE_NOT_FOUND_EXCEPTION_STATUS);
	}

//	private long getRedisCacheId(String token) {
//		// split token on basis of . and stored in an array
//		String[] splitedToken = token.split("\\.");
//		// concatenation of the separated token by removing .
//		String redisTokenKey = splitedToken[1] + splitedToken[2];
//		if (redisTemplate.opsForValue().get(redisTokenKey) == null) {
//			long idForRedis = jwtToken.decodeToken(token);
//			redisTemplate.opsForValue().set(redisTokenKey, idForRedis, 3 * 60, TimeUnit.SECONDS);
//		}
//		return (Long) redisTemplate.opsForValue().get(redisTokenKey);
//	}

	/**
	 * This function takes {@link NoteDTO} as input parameter and token as path
	 * variable. Using token it authorize the user if the user is verified then all
	 * data of noteDto is copied to the note class and creation dateTime and color
	 * information is saved then the user note information is saved in the database.
	 * After successful saving of note it return boolean value.
	 */
	@Override
	public boolean createNote(NoteDTO noteDto, String token) {
		// found authorized user
		User fetchedUser = authenticatedUser(token);
		// implemented redisCache functionality
//		User fetchedUser = userRepository.getUser(getRedisCacheId(token));
		if (fetchedUser != null) {
			Note newNote = new Note();
			BeanUtils.copyProperties(noteDto, newNote);
			newNote.setCreatedDate(LocalDateTime.now());
			newNote.setColor("white");
			fetchedUser.getNotes().add(newNote);
			noteRepository.saveOrUpdate(newNote);
//			elasticSearchRepository.createNote(newNote);
			return true;
		}
		throw new AuthorizationException(Util.USER_AUTHORIZATION_EXCEPTION_MESSAGE,
				Util.USER_AUTHENTICATION_EXCEPTION_STATUS);
	}

	/**
	 * This function takes {@link NoteDTO} as input parameter and token as path
	 * variable. Using token it authorize the user if the user is verified then all
	 * data of noteDto is copied to the note class and update time and date is saved
	 * in the database. On Successful update it display proper message to the user.
	 */
	@Override
	public boolean updateNote(NoteDTO noteDto, long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		Note fetchedNote = verifiedNote(noteId);
		BeanUtils.copyProperties(noteDto, fetchedNote);
		fetchedNote.setUpdatedDate(LocalDateTime.now());
		noteRepository.saveOrUpdate(fetchedNote);
//		elasticSearchRepository.updateNote(fetchedNote);
		return true;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find for the available note on the
	 * database if it found valid note then it delete permanently from database. On
	 * Successful deletion of note it display proper message to the user.
	 */
	@Override
	public boolean deleteNote(long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		verifiedNote(noteId);
		noteRepository.isDeletedNote(noteId);
//		elasticSearchRepository.deleteNote(fetchedVerifiedNote);
		return true;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find for the available of note on
	 * the database. if found valid note then it change the status of archived on
	 * database. if the note is archived already then make it as unArchived note and
	 * after Successful change of archived status of note it return boolean value.
	 */
	@Override
	public boolean isArchivedNote(long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		Note fetchedNote = verifiedNote(noteId);
		// fetched note is not archived
		if (!fetchedNote.isArchived()) {
			fetchedNote.setArchived(true);
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			noteRepository.saveOrUpdate(fetchedNote);
//			elasticSearchRepository.updateNote(fetchedNote);
			return true;
		}
		// if archived already unArchive functionality
		fetchedNote.setArchived(false);
		fetchedNote.setUpdatedDate(LocalDateTime.now());
		noteRepository.saveOrUpdate(fetchedNote);
//		elasticSearchRepository.updateNote(fetchedNote);
		return false;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find for the available of note on
	 * the database. if found valid note then it change the status of pinned on
	 * database. on Successful change of pinned status of note it return boolean
	 * value. if already pinned it will unPin it on second request.
	 */
	@Override
	public boolean isPinnedNote(long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		Note fetchedNote = verifiedNote(noteId);
		if (!fetchedNote.isPinned()) {
			fetchedNote.setPinned(true);
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			noteRepository.saveOrUpdate(fetchedNote);
			return true;
		}
		// if pinned already
		fetchedNote.setPinned(false);
		fetchedNote.setUpdatedDate(LocalDateTime.now());
		noteRepository.saveOrUpdate(fetchedNote);
//		elasticSearchRepository.updateNote(fetchedNote);
		return false;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find for the available of note on
	 * the database. if found valid note then it change the status of trashed on
	 * database. if the note is trashed already then it return false. on Successful
	 * change of trashed status of note it return boolean value.
	 */
	@Override
	public boolean trashNote(long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		Note fetchedNote = verifiedNote(noteId);
		if (!fetchedNote.isTrashed()) {
			fetchedNote.setTrashed(true);
			fetchedNote.setArchived(false);
			fetchedNote.setPinned(false);
			fetchedNote.setRemainderTime(null);
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			noteRepository.saveOrUpdate(fetchedNote);
//			elasticSearchRepository.updateNote(fetchedNote);
			return true;
		}
		// if trashed already
		return false;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find for the available of note on
	 * the database. if found valid note then it change the status of trashed on
	 * database. if the note is trashed already then then it remove the note from
	 * trashed and on Successful change of trashed status of note it return boolean
	 * value.
	 */
	@Override
	public boolean restoreNote(long noteId, String token) {
		// found authorized user
		authenticatedUser(token);
		// verified valid note
		Note fetchedNote = verifiedNote(noteId);
		if (fetchedNote.isTrashed()) {
			fetchedNote.setTrashed(false);
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			noteRepository.saveOrUpdate(fetchedNote);
//			elasticSearchRepository.updateNote(fetchedNote);
			return true;
		}
		return false;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find all the available notes which
	 * are not trashed from database and return it.
	 */
	@Override
	public List<Note> getallNotes(String token) {
		List<Note> fetchedNotes = noteRepository.getAllNotes(authenticatedUser(token).getUserId());
		Collections.sort(fetchedNotes, (note1, note2) -> note2.getCreatedDate().compareTo(note1.getCreatedDate()));
		return fetchedNotes;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find all the available trashed
	 * notes from database and return it.
	 */
	@Override
	public List<Note> getAllTrashedNotes(String token) {
		// note found of authenticated user
		List<Note> fetchedTrashedNotes = noteRepository.getAllTrashedNotes(authenticatedUser(token).getUserId());
		Collections.sort(fetchedTrashedNotes,
				(note1, note2) -> note2.getUpdatedDate().compareTo(note1.getUpdatedDate()));
		if (!fetchedTrashedNotes.isEmpty()) {
			return fetchedTrashedNotes;
		}
		// empty list
		return fetchedTrashedNotes;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find all the available pinned notes
	 * from database and return it.
	 */
	@Override
	public List<Note> getAllPinnedNotes(String token) {
		// note found of authenticated user
		List<Note> fetchedPinnedNotes = noteRepository.getAllPinnedNotes(authenticatedUser(token).getUserId());
		Collections.sort(fetchedPinnedNotes,
				(note1, note2) -> note2.getUpdatedDate().compareTo(note1.getUpdatedDate()));
		if (!fetchedPinnedNotes.isEmpty()) {
			return fetchedPinnedNotes;
		}
		// empty list
		return fetchedPinnedNotes;
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authorization if valid customer then find all the available archived
	 * notes from database and return it.
	 */
	@Override
	public List<Note> getAllArchivedNotes(String token) {
		List<Note> fetchedArchivedNotes = noteRepository.getAllArchivedNotes(authenticatedUser(token).getUserId());
		Collections.sort(fetchedArchivedNotes,
				(note1, note2) -> note2.getUpdatedDate().compareTo(note1.getUpdatedDate()));
		// note found of authenticated user
		if (!fetchedArchivedNotes.isEmpty()) {
			return fetchedArchivedNotes;
		}
		// empty list
		return fetchedArchivedNotes;
	}

	@Override
	public List<Note> getAllRemaindersNotes(String token) {
		List<Note> fetchedremainderNotes = noteRepository.getAllRemainderNotes(authenticatedUser(token).getUserId());
		Collections.sort(fetchedremainderNotes,
				(note1, note2) -> note2.getRemainderTime().compareTo(note1.getRemainderTime()));
		// note found of authenticated user
		if (!fetchedremainderNotes.isEmpty()) {
			return fetchedremainderNotes;
		}
		// empty list
		return fetchedremainderNotes;
	}

	/**
	 * This function takes note id and authorized token and note color from the user
	 * checks for user authentication if valid customer found then it set the color
	 * of the valid fetched note and also record the update time then save to
	 * database.
	 */
	@Override
	public void changeColour(String token, long noteId, String noteColour) {
		// authenticate user
		authenticatedUser(token);
		// validate note
		Note fetchedNote = verifiedNote(noteId);
		fetchedNote.setColor(noteColour);
		noteRepository.saveOrUpdate(fetchedNote);
//		elasticSearchRepository.updateNote(fetchedNote);
	}

	/**
	 * This function takes note id, {@link RemainderDTO} and authorized token from
	 * the user checks for user authentication if valid customer found then it
	 * checks whether remainder is set before or not if not set it set the remainder
	 * for note and add the update time then add to database.
	 */
	@Override
	public void setRemainderforNote(String token, long noteId, String remainderTime) {
		// authenticate user
		authenticatedUser(token);
		// validate note
		Note fetchedNote = verifiedNote(noteId);
		if (fetchedNote.getRemainderTime() == null ||!fetchedNote.getRemainderTime().equals(remainderTime)) {
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			fetchedNote.setRemainderTime(remainderTime);
			noteRepository.saveOrUpdate(fetchedNote);
//			elasticSearchRepository.updateNote(fetchedNote);
			return;
		}
		throw new RemainderException("Opps...Remainder already set for above time", 502);
	}

	/**
	 * This function takes note id and authorized token from the user checks for
	 * user authentication if valid customer found then it checks whether remainder
	 * is set before or not if set it removes remainder for note and add the update
	 * time then add to database.
	 */
	@Override
	public void removeRemainderforNote(String token, long noteId) {
		// authenticate user
		authenticatedUser(token);
		// validate note
		Note fetchedNote = verifiedNote(noteId);
		if (fetchedNote.getRemainderTime() != null) {
			fetchedNote.setRemainderTime(null);
			fetchedNote.setUpdatedDate(LocalDateTime.now());
			noteRepository.saveOrUpdate(fetchedNote);
//			elasticSearchRepository.updateNote(fetchedNote);
			return;
		}
		throw new RemainderException("Opps...Remainder already removed!", 502);
	}

	/**
	 * This function takes note title and authorized token from the user checks for
	 * user authorization if valid customer then find all the available notes which
	 * are not trashed of the authenticated user based on title String.
	 */
	@Override
	public List<Note> searchByTitle(String token, String noteTitle) {
		// authenticate user
		authenticatedUser(token);
		List<Note> fetchedNotes = noteRepository.searchBy(noteTitle);
//		List<Note> fetchedElasticNotes = elasticSearchRepository.searchByTitle(noteTitle);
		// notes are not empty
		if (!fetchedNotes.isEmpty()) {
			return fetchedNotes;
		}
		// if empty
		throw new NoteException(Util.NOTE_NOT_FOUND_EXCEPTION_MESSAGE, Util.NOTE_NOT_FOUND_EXCEPTION_STATUS);
	}

	@Override
	public List<Label> getLabelsOfNote(String token, long noteId) {
		// authenticate user
		authenticatedUser(token);
		return verifiedNote(noteId).getLabelsList();
	}

}
