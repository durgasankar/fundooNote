package com.bridgeLabz.fundooNotes.service.Implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgeLabz.fundooNotes.DTOModel.UserDTO;
import com.bridgeLabz.fundooNotes.repository.IUserRepository;
import com.bridgeLabz.fundooNotes.service.IUserService;
import com.bridgeLabz.fundooNotes.utility.JWTToken;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private IUserRepository userRepository;
	@Autowired
	private JWTToken jwtToken;

	@Override
	public boolean register(UserDTO userDto) {

		return false;
	}

}
