package database.services

import base.Base
import database.services.UserService.EmailAlreadyExistingException

class UserServiceSpec extends Base {
  it("creates a user with UUID") {
    val user = await(userService.create(UserService.CreateData("email", Some("pass"))))

    an[EmailAlreadyExistingException.type] should be thrownBy {
      await(userService.create(UserService.CreateData("email", Some("pass"))))
    }

    val retrieved = await(userService.getById(user.id)).get
    user should be(retrieved)
  }
}
