package app.domain.services;

import app.domain.exceptions.BusinessExceptions;
import app.domain.models.person.Customer;
import app.domain.models.person.User;
import app.domain.ports.CustomerPort;


// RN1: El Número de identificación (DNI/Cédula/NIT) 
// debe ser único para cualquier cliente (Persona Natural o Empresa) 
// en la base de datos.

public class CustomerService {

    private final CustomerPort customerPort;
    private final EmployeeAccessService employeeAccessService;

    public CustomerService(CustomerPort customerPort, EmployeeAccessService employeeAccessService) {
        this.customerPort = customerPort;
        this.employeeAccessService = employeeAccessService;
    }

    public void createClient(Customer customer) {

        // Validar objeto
        if (customer == null) {
            throw new BusinessExceptions("El cliente no puede ser null");
        }

        // Validar identificación
        if (customer.getIdentificationNumber() == null ||
            customer.getIdentificationNumber().trim().isEmpty()) {

            throw new BusinessExceptions("El número de identificación es obligatorio");
        }

        // Normalizar (evita duplicados tipo "123 " vs "123")
        String identification = customer.getIdentificationNumber().trim();

        // Validar unicidad
        Customer existing = customerPort.findByIdentificationNumber(identification);

        if (existing != null) {
            throw new BusinessExceptions("El número de identificación ya existe");
        }

        // Guardar identificación normalizada
        customer.setIdentificationNumber(identification);

        // Guardar cliente
        customerPort.save(customer);
    }

//====================================================================================================

    // ==============================
    // RN27: acceso a clientes
    // ==============================
    public Customer getCustomerInfo(User user, String customerId) {

        // 1. buscar cliente
        Customer customer = customerPort.findByIdentificationNumber(customerId);

        if (customer == null) {
            throw new BusinessExceptions("Cliente no encontrado");
        }

        // 2. validar acceso
        employeeAccessService.validateCommercialCustomerAccess(user, customer);

        // 3. retornar
        return customer;
    }
}

