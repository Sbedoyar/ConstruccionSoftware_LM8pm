package app.domain.services;

import app.domain.exceptions.BusinessException;
import app.domain.models.person.Customer;
import app.domain.models.person.IndividualCustomer;
import app.domain.ports.CustomerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class CreateCustomer {

    private final CustomerPort customerPort;

    @Autowired
    public CreateCustomer(CustomerPort customerPort) {
        this.customerPort = customerPort;
    }

    public void createCustomer(Customer customer) throws BusinessException {

        // Validación general de entrada.
        // No corresponde a una RN numerada, pero evita procesar un cliente nulo.
        if (customer == null) {
            throw new BusinessException("El cliente no puede ser null");
        }

        // RN-01:
        // El número de identificación (DNI/Cédula/NIT) debe ser único
        // para cualquier cliente (Persona Natural o Empresa) en la base de datos.
        // Aquí también se valida que la identificación sea obligatoria.
        validateIdentification(customer.getIdentificationNumber());

        // RN-01:
        // Se valida que no exista previamente otro cliente con la misma identificación.
        validateUniqueIdentification(customer.getIdentificationNumber().trim());

        // RN-AD01:
        // El correo electrónico es obligatorio y debe contener "@" y un dominio válido.
        validateEmail(customer.getEmail());

        // RN-AD02:
        // El número de teléfono es obligatorio y debe tener entre 7 y 15 dígitos/caracteres.
        validatePhone(customer.getPhone());

        // RN-AD03:
        // Si el cliente es persona natural, debe ser mayor de edad (mínimo 18 años).
        validateAdultCustomer(customer);

        // Normalización de datos antes de guardar.
        customer.setIdentificationNumber(customer.getIdentificationNumber().trim());
        customer.setEmail(customer.getEmail().trim());
        customer.setPhone(customer.getPhone().trim());

        // Si todas las reglas se cumplen, se guarda el cliente.
        customerPort.save(customer);
    }

    private void validateIdentification(String identificationNumber) {

        // RN-01:
        // La identificación es obligatoria para cualquier cliente.
        if (identificationNumber == null || identificationNumber.trim().isEmpty()) {
            throw new BusinessException("El número de identificación es obligatorio");
        }
    }

    private void validateUniqueIdentification(String identificationNumber) {

        // RN-01:
        // No puede existir otro cliente con el mismo número de identificación.
        Customer existingCustomer = customerPort.findByIdentificationNumber(identificationNumber);

        if (existingCustomer != null) {
            throw new BusinessException("Ya existe un cliente con ese número de identificación");
        }
    }

    private void validateEmail(String email) {

        // RN-AD01:
        // El correo electrónico es obligatorio.
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("El correo electrónico es obligatorio");
        }

        // RN-AD01:
        // El correo debe tener un formato básico válido: contener "@" y dominio.
        String normalizedEmail = email.trim();
        if (!normalizedEmail.contains("@") || normalizedEmail.lastIndexOf('.') < normalizedEmail.indexOf('@')) {
            throw new BusinessException("El correo electrónico no tiene un formato válido");
        }
    }

    private void validatePhone(String phone) {

        // RN-AD02:
        // El número de teléfono es obligatorio.
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("El número de teléfono es obligatorio");
        }

        // RN-AD02:
        // El teléfono debe tener una longitud mínima de 7 y máxima de 15.
        String normalizedPhone = phone.trim();

        if (normalizedPhone.length() < 7 || normalizedPhone.length() > 15) {
            throw new BusinessException("El número de teléfono debe tener entre 7 y 15 caracteres");
        }
    }

    private void validateAdultCustomer(Customer customer) {

        // RN-AD03:
        // Solo aplica para cliente persona natural.
        if (customer instanceof IndividualCustomer individualCustomer) {
            LocalDate birthDate = individualCustomer.getDateOfBirth();

            // RN-AD03:
            // La fecha de nacimiento es obligatoria para cliente persona natural.
            if (birthDate == null) {
                throw new BusinessException("La fecha de nacimiento es obligatoria para cliente persona natural");
            }

            // RN-AD03:
            // El cliente persona natural debe ser mayor o igual a 18 años.
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) {
                throw new BusinessException("El cliente persona natural debe ser mayor de edad");
            }
        }
    }
}