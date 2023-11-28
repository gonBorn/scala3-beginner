package extensionDemo

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxEither
import cats.syntax.apply.*
import cats.effect.unsafe.implicits.global

object ExtensionDemo:
  case class Person(name: String, age: Int):
    def validateName: Person => Either[String, String] =
      person => Either.cond(person.name.matches("^[a-zA-Z]+$"), person.name, "Error: Name contains special character")

    def validateAge: Person => Either[String, Int] =
      person => Either.cond(
        person.age >= 18 && person.age <= 75,
        person.age,
        "Error: Age should be between 18 and 75"
      )

    def verify[M[_] : Sync]: M[Person] = {
      val value: Validated[CustomError.UnsatisfiedPerson, Person] = (this.validatedGet(validateName), this.validatedGet(validateAge))
        .mapN(Person.apply)
        .leftMap(CustomError.UnsatisfiedPerson.apply)
      Sync[M].fromValidated(value)
    }

  extension[T] (person: Person)
    def validatedGet(filterPeople: Person => Either[String, T]): ValidatedNel[String, T] =
      filterPeople(person).toValidatedNel

  // Extension methods allow one to add methods to a type after the type is defined.
  // https://typelevel.org/cats/datatypes/validated.html
  extension (p: Person)
    def generateId: Int = p.age + 1

  enum CustomError(val message: String) extends Exception:
    case UnsatisfiedPerson(fields: NonEmptyList[String]) extends CustomError(fields.toList.mkString(","))

    override def getMessage: String = message

//  override def run(args: List[String]): IO[ExitCode] =
//    Person("zeyan$$", 1).verify[IO]
//      .as(ExitCode.Success)
//      .handleErrorWith(e => {
//        println(e.getMessage)
//        IO(ExitCode.Error)
//      })

  def main(args: Array[String]): Unit = {
    println(Person("zeyan$$", 1).generateId)

    Person("zeyan$$", 1).verify[IO].handleErrorWith(e => {
        println(e.getMessage)
        IO(ExitCode.Error)
      }).unsafeRunSync()
    Person("zeyan", 1).verify[IO].handleErrorWith(e => {
        println(e.getMessage)
        IO(ExitCode.Error)
      }).unsafeRunSync()
    Person("zeyan", 19).verify[IO].handleErrorWith(e => {
        println(e.getMessage)
        IO(ExitCode.Error)
      }).unsafeRunSync()
  }
