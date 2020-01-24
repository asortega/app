//Paquete main provee todas las funciones disponibles del chaincode
//para ser llamadas en la blockchai
package main

import (
	"embargos/model"
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-chaincode-go/shim"
	pb "github.com/hyperledger/fabric-protos-go/peer"
)

// Chaincode representa nuestro chaincode para la encapsulacion
type Chaincode struct {
}

// Main permite la instalacion del chaincode en un peer
func main() {
	err := shim.Start(new(Chaincode))
	if err != nil {
		fmt.Printf("Error iniciando Chaincode: %s", err)
	}
}

// Init permite la inicializacion del chaincode
// se realiza una carga inicial de datos si es necesario
func (t *Chaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success(nil)
}

// Invoke utilizada para realizar todas las invocaciones del chaincode
func (t *Chaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	fmt.Println("Invocacion corriendo: " + function)
	embargoMapped := model.Embargo{}
	autoridadMapped := model.Autoridad{}
	usuarioMapped := model.Usuario{}
	usuarioSistemaMapped := model.UsuarioSistema{}

	if function == "crearEmbargo" {
		err := json.Unmarshal([]byte(args[0]), &embargoMapped)
		if err != nil {
			return shim.Bad("Información mal estructurada, verificar estructura del embargo en la solicitud.")
		}
		return embargoMapped.CrearEmbargo(stub, args)
	}

	if function == "consultarEmbargo" {
		return embargoMapped.ConsultarEmbargo(stub, args)
	}

	if function == "editarEmbargo" {
		err := json.Unmarshal([]byte(args[0]), &embargoMapped)
		if err != nil {
			return shim.Bad("Información mal estructurada, verificar estructura del embargo en la solicitud.")
		}
		return embargoMapped.EditarEmbargo(stub, args)
	}

	if function == "eliminarEmbargo" {
		return shim.Error("Invocacion prohibida")
	}

	if function == "consultarEmbargoPorCampo" {
		return embargoMapped.ConsultarEmbargoPorCampo(stub, args)
	}

	if function == "historialEmbargo" {
		return embargoMapped.HistorialEmbargo(stub, args)
	}

	if function == "crearAutoridad" {
		json.Unmarshal([]byte(args[0]), &autoridadMapped)
		return autoridadMapped.CrearAutoridad(stub, args)
	}

	if function == "consultarAutoridad" {
		return model.ConsultarAutoridad(stub, args)
	}

	if function == "consultarUsuarioEmbargosAutPag" {
		return model.ConsultarUsuarioEmbargosAutPag(stub, args)
	}

	if function == "editarAutoridad" {
		json.Unmarshal([]byte(args[0]), &autoridadMapped)
		return autoridadMapped.EditarAutoridad(stub, args)
	}

	if function == "eliminarAutoridad" {
		return shim.Error("Invocacion prohibida")
	}

	if function == "consultarAutoridadPorCampo" {
		return autoridadMapped.ConsultarAutoridadPorCampo(stub, args)
	}

	if function == "historialAutoridad" {
		return autoridadMapped.HistorialAutoridad(stub, args)
	}

	if function == "crearUsuario" {
		return usuarioMapped.CrearUsuario(stub, args)
	}

	if function == "consultarUsuario" {
		return usuarioMapped.ConsultarUsuario(stub, args)
	}

	if function == "editarUsuario" {
		return usuarioMapped.EditarUsuario(stub, args)
	}

	if function == "eliminarUsuario" {
		return shim.Error("Invocacion prohibida")
	}

	if function == "consultarUsuarioEmbargosDtePag" {
		return usuarioMapped.ConsultarUsuarioEmbargosDtePag(stub, args)
	}

	if function == "consultarUsuarioEmbargosDteTotal" {
		return usuarioMapped.ConsultarUsuarioEmbargosDteTotal(stub, args)
	}

	if function == "consultarUsuarioPorCampo" {
		return usuarioMapped.ConsultarUsuarioPorCampo(stub, args)
	}

	if function == "historialUsuario" {
		return usuarioMapped.HistorialUsuario(stub, args)
	}

	if function == "crearUsuarioSistema" {
		err := json.Unmarshal([]byte(args[0]), &usuarioSistemaMapped)
		if err != nil {
			return shim.Bad("Información mal estructurada, verificar estructura del embargo en la solicitud.")
		}
		return usuarioSistemaMapped.CrearUsuarioSistema(stub, args)
	}

	if function == "consultarUsuarioSistema" {
		return usuarioSistemaMapped.ConsultarUsuarioSistema(stub, args)
	}

	if function == "editarUsuarioSistema" {
		err := json.Unmarshal([]byte(args[0]), &usuarioSistemaMapped)
		if err != nil {
			return shim.Bad("Información mal estructurada, verificar estructura del embargo en la solicitud.")
		}
		return usuarioSistemaMapped.EditarUsuarioSistema(stub, args)
	}
	fmt.Println("La invocacion no fue encontrada: " + function) //error
	return shim.Error("Se recibio invocacion desconocida")
}
