package com.sublinks.sublinksapi.api.lemmy.v3.user.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtUtil;
import com.sublinks.sublinksapi.api.lemmy.v3.authentication.models.LoginResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.RegistrationMode;
import com.sublinks.sublinksapi.api.lemmy.v3.errorhandler.ApiError;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.CaptchaResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.ChangePassword;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.ChangePasswordEmail;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.DeleteAccount;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.DeleteAccountResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GenerateTotpSecretResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetCaptchaResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.Login;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PasswordResetResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.Register;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.SuccessResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.UpdateTotp;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.UpdateTotpResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.VerifyEmail;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.VerifyEmailResponse;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPersonTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.email.entities.Email;
import com.sublinks.sublinksapi.email.enums.EmailTemplatesEnum;
import com.sublinks.sublinksapi.email.services.EmailService;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.Captcha;
import com.sublinks.sublinksapi.person.entities.PasswordReset;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonEmailVerification;
import com.sublinks.sublinksapi.person.entities.PersonRegistrationApplication;
import com.sublinks.sublinksapi.person.enums.PersonRegistrationApplicationStatus;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.CaptchaService;
import com.sublinks.sublinksapi.person.services.PasswordResetService;
import com.sublinks.sublinksapi.person.services.PersonEmailVerificationService;
import com.sublinks.sublinksapi.person.services.PersonRegistrationApplicationService;
import com.sublinks.sublinksapi.person.services.PersonService;
import com.sublinks.sublinksapi.person.services.UserDataService;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterBlockedException;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterReportException;
import com.sublinks.sublinksapi.slurfilter.services.SlurFilterService;
import com.sublinks.sublinksapi.utils.TotpUtil;
import com.sublinks.sublinksapi.utils.models.LemmyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/user")
@Tag(name = "User")
public class UserAuthController extends AbstractLemmyApiController {

  private static final Logger logger = LoggerFactory.getLogger(UserAuthController.class);
  private final JwtUtil lemmyJwtUtil;
  private final PersonService personService;
  private final PersonRepository personRepository;
  private final LocalInstanceContext localInstanceContext;
  private final PersonRegistrationApplicationService personRegistrationApplicationService;
  private final SlurFilterService slurFilterService;
  private final CaptchaService captchaService;
  private final RolePermissionService rolePermissionService;
  private final ConversionService conversionService;
  private final EmailService emailService;
  private final UserDataService userDataService;
  private final PasswordResetService passwordResetService;
  private final PersonEmailVerificationService personEmailVerificationService;

  @Operation(summary = "Register a new user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginResponse.class, description =
          "JWT will be empty if registration "
              + "requires email verification or application approval."))}),
      @ApiResponse(responseCode = "400", description = "Passwords do not match.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))}),
      @ApiResponse(responseCode = "400", description = "Username is taken.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))}),
      @ApiResponse(responseCode = "400", description = "Captcha is incorrect.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LemmyException.class))}),
      @ApiResponse(responseCode = "400", description = "Person is blocked by slur filter.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("register")
  LoginResponse create(final HttpServletRequest request,
      @Valid @RequestBody final Register registerForm) throws LemmyException {

    InstanceConfig instanceConfig = localInstanceContext.instance().getInstanceConfig();

    if (instanceConfig != null) {
      if (instanceConfig.getRegistrationMode() == RegistrationMode.Closed) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "registration_closed");
      }
      if (instanceConfig.isCaptchaEnabled()) {
        if (!captchaService.validateCaptcha(registerForm.captcha_answer(), true)) {
          throw new LemmyException("captcha_incorrect", HttpStatus.BAD_REQUEST);
        }
      }
    }

    if (personRepository.findOneByNameIgnoreCase(registerForm.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username_taken");
    }
    try {
      String filteredUsername = slurFilterService.censorText(registerForm.username());
      if (!Objects.equals(filteredUsername, registerForm.username())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_blocked_by_slur_filter");
      }
    } catch (SlurFilterBlockedException | SlurFilterReportException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_blocked_by_slur_filter");
    }

    final Person person = personService.getDefaultNewUser(registerForm.username());
    person.setEmail(registerForm.email());
    person.setPassword(registerForm.password());
    if (!Objects.equals(registerForm.password(), registerForm.password_verify())) {
      // @todo throw lemmy error code
      throw new RuntimeException("Passwords do not match");
    }
    personService.createPerson(person);
    String token = lemmyJwtUtil.generateToken(person);

    boolean send_verification_email = false;

    if (instanceConfig != null) {
      if (instanceConfig.getRegistrationMode() == RegistrationMode.RequireApplication) {

        personRegistrationApplicationService.createPersonRegistrationApplication(
            PersonRegistrationApplication.builder().applicationStatus(
                PersonRegistrationApplicationStatus.pending).person(person).question(
                instanceConfig.getRegistrationQuestion()).answer(registerForm.answer()).build());
        token = "";
      }

      if (instanceConfig.isRequireEmailVerification()) {
        if (person.getEmail() == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_required");
        }

        send_verification_email = true;
        token = "";

        PersonEmailVerification personEmailVerification = personEmailVerificationService.create(
            person, request.getRemoteAddr(), request.getHeader("User-Agent"));

        Map<String, Object> params = emailService.getDefaultEmailParameters();

        params.put("person", person);
        params.put("verificationUrl", localInstanceContext.instance().getDomain() + "/verify_email/"
            + personEmailVerification.getToken());
        try {
          final String template_name = EmailTemplatesEnum.VERIFY_EMAIL.toString();
          emailService.saveToQueue(Email.builder()
              .personRecipients(List.of(person))
              .subject(emailService.getSubjects().get(template_name).getAsString())
              .htmlContent(emailService.formatTextEmailTemplate(template_name,
                  new Context(Locale.getDefault(), params)))
              .textContent(emailService.formatEmailTemplate(template_name,
                  new Context(Locale.getDefault(), params)))
              .build());

        } catch (Exception e) {
          personRepository.delete(person);
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "email_sending_failed");
        }
      }
    } else if (person.getEmail() != null && !person.getEmail().isEmpty()) {
      Map<String, Object> properties = emailService.getDefaultEmailParameters();
      properties.put("person", person);

      try {
        final Context context = new Context(Locale.getDefault(), properties);

        final String template_name = EmailTemplatesEnum.REGISTRATION_SUCCESS.toString();
        emailService.saveToQueue(Email.builder()
            .personRecipients(List.of(person))
            .subject(emailService.getSubjects().get(template_name).getAsString())
            .htmlContent(emailService.formatTextEmailTemplate(template_name, context))
            .textContent(emailService.formatEmailTemplate(template_name, context))
            .build());
      } catch (IOException e) {
        personRepository.delete(person);
        logger.error("Error reading Subjects!", e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "email_sending_failed");
      }
    }

    if (token != null && !token.isEmpty()) {
      userDataService.checkAndAddIpRelation(person, request.getRemoteAddr(), token,
          request.getHeader("User-Agent"));
    }
    person.setEmailVerified(!send_verification_email);
    personService.updatePerson(person);
    return LoginResponse.builder().jwt(token).registration_created(true).verify_email_sent(
        send_verification_email).build();
  }

  @Operation(summary = "Fetch a Captcha.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetCaptchaResponse.class))})})
  @GetMapping("get_captcha")
  GetCaptchaResponse captcha() {

    if (!localInstanceContext.instance().getInstanceConfig().isCaptchaEnabled()) {
      return GetCaptchaResponse.builder().build();
    }
    Captcha captcha = captchaService.getCaptcha();
    return GetCaptchaResponse.builder().ok(
        conversionService.convert(captcha, CaptchaResponse.class)).build();
  }

  @Operation(summary = "Log into lemmy.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetCaptchaResponse.class))}),
      @ApiResponse(responseCode = "400", description = "A valid user is not found or password is incorrect.", content = {
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("login")
  LoginResponse login(final HttpServletRequest request, @Valid @RequestBody final Login loginForm)
      throws LemmyException {

    final Person person = personRepository.findOneByNameIgnoreCase(loginForm.username_or_email())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    // @todo verify password

    if (person.isDeleted()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user_deleted");
    }

    if (!person.isEmailVerified()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_not_verified");
    }

    String totpSecret = person.getTotpVerifiedSecret();
    if (totpSecret != null) {
      if (loginForm.totp_2fa_token() == null) {
        throw new LemmyException("missing_totp_token", HttpStatus.BAD_REQUEST);
      }
      if (!TotpUtil.verify(totpSecret, loginForm.totp_2fa_token())) {
        throw new LemmyException("wrong_totp_token", HttpStatus.BAD_REQUEST);
      }
    }

    if (!personService.isValidPersonPassword(person, loginForm.password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password_incorrect");
    }

    final String token = lemmyJwtUtil.generateToken(person);

    userDataService.checkAndAddIpRelation(person, request.getRemoteAddr(), token,
        request.getHeader("User-Agent"));

    return LoginResponse.builder().jwt(token).registration_created(
            false) // @todo return true if application created
        .verify_email_sent(false) // @todo return true if welcome email sent for verification
        .build();
  }

  @Operation(summary = "Delete your account.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DeleteAccountResponse.class))})})
  @PostMapping("delete_account")
  DeleteAccountResponse delete(@RequestBody final DeleteAccount deleteAccount,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    if (!personService.isValidPersonPassword(person, deleteAccount.password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password_incorrect");
    }

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.DELETE_USER);

    // Bug in the Lemmy UI delete_content() is always false but the api is just assuming true..., so we just ignore it
    // @todo check when lemmy fixes this
    personService.deleteUserAccount(person, true);

    return DeleteAccountResponse.builder().build();
  }

  @Operation(summary = "Reset your password.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PasswordResetResponse.class))})})
  @PostMapping("password_reset")
  PasswordResetResponse passwordReset(
      @RequestBody final com.sublinks.sublinksapi.api.lemmy.v3.user.models.PasswordReset passwordResetForm) {

    Optional<Person> person = personRepository.findOneByEmail(passwordResetForm.email());

    // @todo: implement reset password and check for 2fa

    if (person.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_not_found");
    }
    final String template_name = EmailTemplatesEnum.PASSWORD_RESET.toString();

    Person foundPerson = person.get();

    if (foundPerson.getEmail() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_required");
    }

    if (passwordResetService.isRateLimited(foundPerson)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rate_limited");
    }

    PasswordReset passwordReset = passwordResetService.createPasswordReset(foundPerson);

    try {
      Map<String, Object> params = emailService.getDefaultEmailParameters();

      params.put("person", foundPerson);

      String url = localInstanceContext.instance().getDomain().substring(0,
          localInstanceContext.instance().getDomain().length() - 4) + "/password_change/"
          + passwordReset.getToken();

      // #todo: implement the password reset in the frontend
      params.put("resetUrl", url);

      emailService.saveToQueue(Email.builder()
          .personRecipients(List.of(foundPerson))
          .subject(emailService.getSubjects().get(template_name).getAsString())
          .htmlContent(emailService.formatTextEmailTemplate(template_name,
              new Context(Locale.getDefault(), params)))
          .textContent(emailService.formatEmailTemplate(template_name,
              new Context(Locale.getDefault(), params)))
          .build());
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "email_sending_failed");
    }

    return PasswordResetResponse.builder().build();
  }

  @Operation(summary = "Change your password from an email / token based reset.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginResponse.class))})})
  @PostMapping("password_change")
  LoginResponse passwordChange(final HttpServletRequest request,
      @RequestBody final ChangePasswordEmail changePasswordForm) {

    Optional<PasswordReset> passwordReset = passwordResetService.findFirstByToken(
        changePasswordForm.token());

    if (passwordReset.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token_invalid");
    }
    PasswordReset passwordResetEntity = passwordReset.get();

    if (passwordResetEntity.isUsed()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token_used");
    }

    if (passwordResetEntity.getPerson() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token_invalid");
    }

    Person person = passwordResetEntity.getPerson();

    if (!Objects.equals(changePasswordForm.password(), changePasswordForm.password_verify())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passwords_do_not_match");
    }

    passwordResetEntity.setUsed(true);
    person.setPassword(personService.encodePassword(changePasswordForm.password()));

    passwordResetService.updatePasswordReset(passwordResetEntity);
    personService.updatePerson(person);

    userDataService.invalidateAllUserData(person);
    String token = lemmyJwtUtil.generateToken(person);

    userDataService.checkAndAddIpRelation(person, request.getRemoteAddr(), token,
        request.getHeader("User-Agent"));

    return LoginResponse.builder().jwt(token).build();
  }

  @Operation(summary = "Verify your email.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VerifyEmailResponse.class))})})
  @PostMapping("verify_email")
  SuccessResponse verifyEmail(@Valid @RequestBody VerifyEmail verifyEmailForm) {

    Optional<PersonEmailVerification> personEmailVerification = personEmailVerificationService.getActiveVerificationByToken(
        verifyEmailForm.token());

    if (personEmailVerification.isEmpty()) {
      return SuccessResponse.builder().success(false).error("token_not_found").build();
    }

    PersonEmailVerification personEmailVerificationEntity = personEmailVerification.get();

    if (personEmailVerificationEntity.getPerson() == null) {
      return SuccessResponse.builder().success(false).error("person_not_found").build();
    }

    Person person = personEmailVerificationEntity.getPerson();

    person.setEmailVerified(true);
    personService.updatePerson(person);

    personEmailVerificationEntity.setActive(false);
    personEmailVerificationService.update(personEmailVerificationEntity);
    Map<String, Object> properties = emailService.getDefaultEmailParameters();
    properties.put("person", person);

    try {
      final Context context = new Context(Locale.getDefault(), properties);

      final String template_name = EmailTemplatesEnum.REGISTRATION_SUCCESS.toString();
      emailService.saveToQueue(Email.builder()
          .personRecipients(List.of(person))
          .subject(emailService.getSubjects().get(template_name).getAsString())
          .htmlContent(emailService.formatTextEmailTemplate(template_name, context))
          .textContent(emailService.formatEmailTemplate(template_name, context))
          .build());
    } catch (IOException e) {
      logger.error("Error reading Subjects!", e);
    }
    return SuccessResponse.builder().success(true).build();
  }

  @Operation(summary = "Change your user password.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginResponse.class))})})
  @PutMapping("change_password")
  LoginResponse changePassword(final HttpServletRequest request,
      @RequestBody final ChangePassword changePasswordForm, final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.RESET_PASSWORD);

    if (!personService.isValidPersonPassword(person, changePasswordForm.old_password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password_incorrect");
    }

    if (!Objects.equals(changePasswordForm.new_password(),
        changePasswordForm.new_password_verify())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passwords_do_not_match");
    }

    person.setPassword(personService.encodePassword(changePasswordForm.new_password()));

    personService.updatePerson(person);
    userDataService.invalidateAllUserData(person);

    String token = lemmyJwtUtil.generateToken(person);

    userDataService.checkAndAddIpRelation(person, request.getRemoteAddr(), token,
        request.getHeader("User-Agent"));

    return LoginResponse.builder().jwt(token).build();
  }

  @Operation(summary = "Generate a TOTP / two-factor secret.\r\r Afterwards you need to call `/user/totp/update` with a valid token to enable it.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GenerateTotpSecretResponse.class))})})
  @PostMapping("totp/generate")
  GenerateTotpSecretResponse totpGenerate(final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    try {
      String secret = TotpUtil.createSecretString(160);
      GenerateTotpSecretResponse generateTotpSecretResponse = GenerateTotpSecretResponse.builder()
          .totp_secret_url(
              TotpUtil.createUri(localInstanceContext.instance().getDomain(), person.getName(),
                  secret).toString()

          )
          .build();

      person.setTotpSecret(secret);
      personService.updatePerson(person);

      return generateTotpSecretResponse;

    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "totp_secret_generation_failed");
    }
  }

  @Operation(summary =
      "Enable / Disable TOTP / two-factor authentication.\r\r To enable, you need to first call "
          + "`/user/totp/generate` and then pass a valid token to this.\r\r "
          + "Disabling is only possible if 2FA was previously enabled. Again it is necessary to pass a valid token.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateTotpResponse.class))})})
  @PostMapping("totp/update")
  UpdateTotpResponse totpUpdate(@Valid @RequestBody final UpdateTotp updateTotpForm,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    UpdateTotpResponse.UpdateTotpResponseBuilder updateTotpResponseBuilder = UpdateTotpResponse.builder();

    if (updateTotpForm.enabled()) {
      if (person.getTotpSecret() == null) {
        updateTotpResponseBuilder.enabled(false);
        return updateTotpResponseBuilder.build();
      }
      if (!TotpUtil.verify(person.getTotpSecret(), updateTotpForm.totp_token())) {
        updateTotpResponseBuilder.enabled(false);
        return updateTotpResponseBuilder.build();
      }
      person.setTotpVerifiedSecret(person.getTotpSecret());
      person.setTotpSecret(null);
    } else {
      if (TotpUtil.verify(person.getTotpVerifiedSecret(), updateTotpForm.totp_token())) {
        updateTotpResponseBuilder.enabled(false);
        return updateTotpResponseBuilder.build();
      }
      person.setTotpVerifiedSecret(null);
    }
    personService.updatePerson(person);

    return updateTotpResponseBuilder.enabled(true).build();
  }

  @Operation(summary = "Validates your Token, throws an error if it is invalid.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateTotpResponse.class))})})
  @GetMapping("validate_auth")
  SuccessResponse validate_auth(final JwtPerson principal) {

    Optional<Person> person = getOptionalPerson(principal);

    return SuccessResponse.builder().success(person.isPresent()).error(
        person.isPresent() ? null : "not_logged_in").build();
  }
}
