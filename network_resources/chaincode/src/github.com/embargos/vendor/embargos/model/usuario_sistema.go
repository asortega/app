package model

import (
	"embargos/utils"
	"fmt"

	"github.com/hyperledger/fabric-chaincode-go/shim"
	pb "github.com/hyperledger/fabric-protos-go/peer"
)

// Reprensenta una Persona en la blockchain, utilizada para la herencia de atributos
type UsuarioSistema struct {
	Persona
	Username        string   `json:"username"`
	Password        string   `json:"password"`
	ConfirmPassword string   `json:"confirmPassword"`
	Roles           []string `json:"roles"`
	OwnedBy         string   `json:"ownedby"`
	Habilitado      bool     `json:"habilitado"`
	IdAutoridad     string   `json:"idAutoridad"`
}

func (u *UsuarioSistema) CrearUsuarioSistema(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	fmt.Println("Registrando usuario de sistema")

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	// === Verificar  si existe el usuario===
	if err := ExisteUsuarioSistema(stub, u.Username); err.Status != shim.OK {
		return err
	}

	err := stub.PutState(u.Username, []byte(args[0]))
	if err != nil {
		return shim.Error("Fallo al guardar el usuario en la blockchain, intente de nuevo o comuniquese con soporte.")
	}
	return shim.Success(nil)
}

func (e *UsuarioSistema) ConsultarUsuarioSistema(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var username string
	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	username = args[0]
	valAsbytes, err := stub.GetState(username)
	if err != nil {
		return shim.Error("Fallo al buscar el usuario en la blockchain, intente de nuevo o comuniquese con soporte.")
	} else if valAsbytes == nil {
		return shim.Success(nil)
	}
	return shim.Success(valAsbytes)
}

func (u *UsuarioSistema) EditarUsuarioSistema(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	fmt.Println("Registrando usuario de sistema")

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	err := stub.PutState(u.Username, []byte(args[0]))
	if err != nil {
		return shim.Error("Fallo al guardar el usuario en la blockchain, intente de nuevo o comuniquese con soporte.")
	}
	return shim.Success(nil)
}

//Retorna ERROR si el usuario existe
func ExisteUsuarioSistema(stub shim.ChaincodeStubInterface, key string) pb.Response {
	usuarioAsBytes, err := stub.GetState(key)
	if err != nil {
		return shim.Error("Fallo al buscar el usuario en la blockchain, intente de nuevo o comuniquese con soporte.")
	} else if usuarioAsBytes == nil {
		return shim.Success(nil)
	}
	return shim.Conflict("Embargo ya existente en la blockchain, verificar informacion del embargo.")
}
