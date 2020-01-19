package model

import (
	"bytes"
	"embargos/utils"
	"fmt"
	"strconv"
	"time"

	"github.com/hyperledger/fabric-chaincode-go/shim"
	pb "github.com/hyperledger/fabric-protos-go/peer"
)

// Reprensenta un embargo en la blockchain, utilizada para la deserializacion y serializacion
// de un embargo en formato JSON
type Embargo struct {
	Username         string       `json:"username"`
	NumProceso       string       `json:"numProceso"`
	IdAutoridad      string       `json:"idAutoridad"`
	NumOficio        string       `json:"numOficio,omitempty"`
	FechaOficio      utils.Fecha  `json:"fechaOficio"`
	TipoEmbargo      string       `json:"tipoEmbargo"`
	MontoAEmbargar   float64      `json:"montoAEmbargar,omitempty"`
	NumCuentaAgrario string       `json:"numCuentaAgrario"`
	EmbargoProcesado bool         `json:"embargoProcesado"`
	Embargado        bool         `json:"embargado"`
	Demandantes      []Demandante `json:"demandantes,omitempty"`
	Demandados       []Demandado  `json:"demandados"`
}

// CrearEmbargo crea un embargo en la blockchain asociando y registrando el embargo para los demandantes,demandados y la autoridad.
// En caso de que la autoridad no exista se retornara un error. En caso de que un demandante y/o un demandado
// no exista en la blockchain se procedera a crear el demandante y/o demandado y posteriormente a asociar el embargo.
// Recibe: Stub
// Recibe: Args especificando en la posicion 0 del arreglo el embargo en formato Json
// Retorna: Error si la autoridad no existe o si no se aprobo la transaccion, Success si se registro el embargo
func (e *Embargo) CrearEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	//TO DO
	//Mirar como hacer un rollback
	autoridad := Autoridad{}

	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	// === Verificar  si NO existe el embargo===
	if err := ExisteEmbargo(stub, e.NumProceso); err.Status != shim.OK {
		return err
	}

	// === Verificar si existe la autoridad ===
	fmt.Println("Verificando autoridad")
	err, autoridadJSONasBytes := autoridad.NoExisteAutoridad(stub, e.IdAutoridad)
	if err.Status != shim.OK {
		return err
	}
	fmt.Println("Autoridad verificada")

	// === Guardar el embargo ===
	fmt.Println("Registrando embargo")
	if err := e.guardarEmbargo(stub, args); err.Status != shim.OK {
		return err
	}
	fmt.Println("Embargo creado")

	// === Registrar demandantes ===
	fmt.Println("Registrando demandantes")
	if err := e.registrarEmbargoEnDemandantes(stub); err.Status != shim.OK {
		return err
	}
	fmt.Println("Demandantes registrados")

	// === Registrar demandados ===
	fmt.Println("Registrando demandados")
	if err := e.registrarEmbargoEnDemandados(stub); err.Status != shim.OK {
		return err
	}
	fmt.Println("Demandados registrados")

	//=== Registrar embargo para autoridad ===
	fmt.Println("Registrando embargo para autoridad")
	if err := autoridad.RegistrarEmbargoEnAutoridad(stub, autoridadJSONasBytes, e.NumProceso); err.Status != shim.OK {
		return err
	}
	fmt.Println("Embargo registrado en la autoridad")

	return shim.Success(nil)
}

// EditarEmbargo edita un embargo en la blockchain asociando y registrando el embargo para los demandantes,demandados y la autoridad.
// En caso de que la autoridad no exista se retornara un error. En caso de que un demandante y/o un demandado
// no exista en la blockchain se procedera a crear el demandante y/o demandado y posteriormente a asociar el embargo.
// Recibe: Stub
// Recibe: Args especificando en la posicion 0 del arreglo el embargo en formato Json
// Retorna: Error si la autoridad no existe o si no se aprobo la transaccion, Success si se registro el embargo
func (e *Embargo) EditarEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	autoridad := Autoridad{}

	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	// === Verificar si existe la autoridad ===
	fmt.Println("Verificando autoridad")
	err, autoridadJSONasBytes := autoridad.NoExisteAutoridad(stub, e.IdAutoridad)
	if err.Status != shim.OK {
		return err
	}
	fmt.Println("Autoridad verificada")

	// === Guardar el embargo ===
	fmt.Println("Registrando embargo")
	if err := e.guardarEmbargo(stub, args); err.Status != shim.OK {
		return err
	}
	fmt.Println("Embargo creado")

	// === Registrar demandantes ===
	fmt.Println("Registrando demandantes")
	if err := e.registrarEmbargoEnDemandantes(stub); err.Status != shim.OK {
		return err
	}
	fmt.Println("Demandantes registrados")

	// === Registrar demandados ===
	fmt.Println("Registrando demandados")
	if err := e.registrarEmbargoEnDemandados(stub); err.Status != shim.OK {
		return err
	}
	fmt.Println("Demandados registrados")

	// === Registrar embargo para autoridad ===
	fmt.Println("Registrando embargo para autoridad")
	if err := autoridad.RegistrarEmbargoEnAutoridad(stub, autoridadJSONasBytes, e.NumProceso); err.Status != shim.OK {
		return err
	}
	fmt.Println("Embargo registrado en la autoridad")

	return shim.Success(nil)
}

// GuardarEmbargo permite la interaccion directa con la blockchain guardando el embargo como bytes
// Recibe: Stub
// Recibe: Args especificando en la  posicion 0 del arreglo el embargo en formato Json
// Retorna: Error si no se aprobo la transaccion, Success si se registro el embargo
func (e *Embargo) guardarEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	err := stub.PutState(e.NumProceso, []byte(args[0]))
	if err != nil {
		return shim.Error("Fallo al guardar el embargo en la Blockchain, intente de nuevo o comuniquese con soporte.")
	}
	return shim.Success(nil)
}

// RegistrarEmbargoEnDemandantes permite registrar un embargo en la blockchain para todos los demandantes
// Recibe: Stub
// Recibe: Demandantes a registrar el embargo
// Retorna: Error si no se aprobo la transaccion, Success si se registro el embargo en los demandantes
func (e *Embargo) registrarEmbargoEnDemandantes(stub shim.ChaincodeStubInterface) pb.Response {
	// === Registrar demandantes ===
	fmt.Println("Registrando embargo a los demandantes")
	for _, demandante := range e.Demandantes {
		fmt.Println("Registrando embargo para: " + demandante.Identificacion)
		key, err := stub.CreateCompositeKey("idPersona", []string{demandante.Identificacion, demandante.TipoIdentificacion})
		if err != nil {
			return shim.Error("Fallo en la creación de la llave compuesta de usuario, intente de nuevo o comuniquese con soporte.")
		}
		usuarioJSONBytes, err := stub.GetState(key)
		usuario := Usuario{}
		if err != nil {
			return shim.Error("Fallo al guardar usuario en la Blockchain, intente de nuevo o comuniquese con soporte.")
		} else if usuarioJSONBytes == nil {
			fmt.Println("La persona no existe en la Blockchain")
			usuario = Usuario{Persona{demandante.Identificacion, demandante.TipoIdentificacion, demandante.Nombres, demandante.Apellidos}, []string{e.NumProceso}, []string{}}
			usuario.registrarUsuarioConEmbargo(stub)
			fmt.Println("Persona registrada y embargo registrado")
		} else {
			fmt.Println("La persona si existe en la Blockchain")
			usuario.agregarEmbargoEnUsuario(stub, usuarioJSONBytes, e.NumProceso, true)
			fmt.Println("Embargo registrado para: " + demandante.Identificacion)
		}
	}
	return shim.Success(nil)
}

// RegistrarEmbargoEnDemandados permite registrar un embargo en la blockchain para todos los demandados
// Recibe: Stub
// Recibe: Demandados a registrar el embargo
// Retorna: Error si no se aprobo la transaccion, Success si se registro el embargo en los demandados
func (e *Embargo) registrarEmbargoEnDemandados(stub shim.ChaincodeStubInterface) pb.Response {
	// === Registrar demandados ===
	for _, demandado := range e.Demandados {
		fmt.Println("Registrando embargo para: " + demandado.Identificacion)
		key, err := stub.CreateCompositeKey("idPersona", []string{demandado.Identificacion, demandado.TipoIdentificacion})
		if err != nil {
			return shim.Error("Fallo en la creación de la llave compuesta de usuario, intente de nuevo o comuniquese con soporte.")
		}
		usuarioJSONBytes, err := stub.GetState(key)
		var usuario Usuario
		if err != nil {
			return shim.Error("Fallo al guardar usuario en la Blockchain, intente de nuevo o comuniquese con soporte.")
		} else if usuarioJSONBytes == nil {
			fmt.Println("La persona no existe en la Blockchain")
			usuario = Usuario{Persona{demandado.Identificacion, demandado.TipoIdentificacion, demandado.Nombres, demandado.Apellidos}, []string{}, []string{e.NumProceso}}
			usuario.registrarUsuarioConEmbargo(stub)
			fmt.Println("Persona registrada y embargo registrado")
		} else {
			fmt.Println("La persona si existe en la Blockchain")
			usuario.agregarEmbargoEnUsuario(stub, usuarioJSONBytes, e.NumProceso, false)
			fmt.Println("Embargo registrado para: " + demandado.Identificacion)
		}
	}
	return shim.Success(nil)
}

// ConsultarEmbargo permite la consulta de un embargo por el campo "idEmbargo", consulta de tipo GetState
// Recibe: Stub
// Recibe: Args especificando en la posicion 0 del arreglo la consulta a realizar en formato HyperledgerFabric
// Retorna: El embargo consultado en bytes, Error si no se aprobo la transaccion
func (e *Embargo) ConsultarEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var idEmbargo string
	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	idEmbargo = args[0]
	valAsbytes, err := stub.GetState(idEmbargo)
	if err != nil {
		return shim.Error("Fallo al buscar el embargo en la blockchain, intente de nuevo o comuniquese con soporte.")
	} else if valAsbytes == nil {
		return shim.Success(nil)
	}
	return shim.Success(valAsbytes)
}

// ConsultarEmbargoPorCampo permite la consulta de un embargo por cualquier campo, consulta de tipo RichQuery
// Recibe: Stub
// Recibe: Args especificando en la posicion 0 del arreglo la consulta a realizarse
// Retorna: El embargo consultado en bytes, Error si no se aprobo la transaccion
func (e *Embargo) ConsultarEmbargoPorCampo(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	queryString := args[0]

	queryResults, err := utils.ConsultaPorString(stub, queryString)
	if err != nil {
		return shim.Error("Fallo en realizar la consulta, intente de nuevo o comuniquese con soporte.")
	}
	return shim.Success(queryResults)
}

// EliminarEmbargo elimina un embargo del WORLD STATE
// Recibe: Stub
// Recibe: Args especificando en la posicion 0 del arreglo el id del embargo
// Retorna: El embargo consultado en bytes, Error si no se aprobo la transaccion
func (e *Embargo) EliminarEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	idEmbargo := args[0]

	// === Eliminar embargo ===
	err := stub.DelState(idEmbargo)
	if err != nil {
		return shim.Error("Failed to delete state:" + err.Error())
	}
	return shim.Success(nil)
}

func (e *Embargo) HistorialEmbargo(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	fmt.Println("Se recibio los siguientes parametros: " + args[0])

	// === Verificar argumentos ===
	if err := utils.InputSanation(args, 1); err.Status != shim.OK {
		return err
	}

	idEmbargo := args[0]

	fmt.Printf("- start getHistory: %s\n", idEmbargo)

	resultsIterator, err := stub.GetHistoryForKey(idEmbargo)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing historic values for the marble
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		response, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"TxId\":")
		buffer.WriteString("\"")
		buffer.WriteString(response.TxId)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Value\":")
		// if it was a delete operation on given key, then we need to set the
		//corresponding value null. Else, we will write the response.Value
		//as-is (as the Value itself a JSON marble)
		if response.IsDelete {
			buffer.WriteString("null")
		} else {
			buffer.WriteString(string(response.Value))
		}

		buffer.WriteString(", \"Timestamp\":")
		buffer.WriteString("\"")
		buffer.WriteString(time.Unix(response.Timestamp.Seconds, int64(response.Timestamp.Nanos)).String())
		buffer.WriteString("\"")

		buffer.WriteString(", \"IsDelete\":")
		buffer.WriteString("\"")
		buffer.WriteString(strconv.FormatBool(response.IsDelete))
		buffer.WriteString("\"")

		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")

	fmt.Printf("- getHistoryForMarble returning:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}

//Funcion para verificar si existe un embargo
//Retorna ERROR si ya existe el embargo
func ExisteEmbargo(stub shim.ChaincodeStubInterface, idEmbargo string) pb.Response {
	embargoAsBytes, err := stub.GetState(idEmbargo)
	if err != nil {
		return shim.Error("Fallo al buscar el embargo en la blockchain, intente de nuevo o comuniquese con soporte.")
	} else if embargoAsBytes == nil {
		fmt.Println("El embargo no existe")
		return shim.Success(nil)
	}
	return shim.Conflict("Embargo ya existente en la blockchain, verificar informacion del embargo.")
}
