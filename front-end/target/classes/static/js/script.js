
$(document)
		.ready(
				function() {

					// Tabla1 Consultar Judicial
					$('table.tb1const-judicial')
							.DataTable(
									{
										"dom" : "<'row'<'col-sm-12 col-md-4 toolbar1'><'col-sm-12 col-md-4'l><'col-sm-12 col-md-4'p>>"
												+ "<'row'<'col-sm-12'tr>>",
										"pageLength" : 2,
										"language" : {
											"paginate" : {
												"previous" : "Anterior",
												"next" : "Siguiente"
											},
											"lengthMenu" : 'Mostrar <select>'
													+ '<option value="2">2</option>'
													+ '<option value="5">5</option>'
													+ '<option value="10">10</option>'
													+ '<option value="-1">Todo</option>'
													+ '</select> registros'
										}
									});
					// Tabla2 Consultar Judicial
					$('table.tb2const-judicial')
							.DataTable(
									{
										"dom" : "<'row'<'col-sm-12 col-md-4 toolbar2'><'col-sm-12 col-md-4'l><'col-sm-12 col-md-4'p>>"
												+ "<'row'<'col-sm-12'tr>>",
										"pageLength" : 2,
										"language" : {
											"paginate" : {
												"previous" : "Anterior",
												"next" : "Siguiente"
											},
											"lengthMenu" : 'Mostrar <select>'
													+ '<option value="2">2</option>'
													+ '<option value="5">5</option>'
													+ '<option value="10">10</option>'
													+ '<option value="-1">Todo</option>'
													+ '</select> registros'
										}
									});

					// Tabla1 Crear Coactivo
					$('table.tb1main-coactivo')
							.DataTable(
									{
										"dom" : "<'row'<'col-sm-12 col-md-4 toolbar2'><'col-sm-12 col-md-4'l><'col-sm-12 col-md-4'p>>"
												+ "<'row'<'col-sm-12'tr>>",
										"pageLength" : 5,
										"language" : {
											"paginate" : {
												"previous" : "Anterior",
												"next" : "Siguiente"
											},
											"lengthMenu" : 'Mostrar <select>'
													+ '<option value="5">5</option>'
													+ '<option value="10">10</option>'
													+ '<option value="20">20</option>'
													+ '<option value="-1">Todo</option>'
													+ '</select> registros'
										}
									});

					// Tabla2 Consultar Coactivo
					$('table.tb1const-coactivo')
							.DataTable(
									{
										"dom" : "<'row'<'col-sm-12 col-md-4 toolbar2'><'col-sm-12 col-md-4'l><'col-sm-12 col-md-4'p>>"
												+ "<'row'<'col-sm-12'tr>>",
										"pageLength" : 5,
										"language" : {
											"paginate" : {
												"previous" : "Anterior",
												"next" : "Siguiente"
											},
											"lengthMenu" : 'Mostrar <select>'
													+ '<option value="5">5</option>'
													+ '<option value="10">10</option>'
													+ '<option value="20">20</option>'
													+ '<option value="-1">Todo</option>'
													+ '</select> registros'
										}
									});
					$("table.tbusuarios")
					.DataTable(
							{
								"dom" : "<'row'<'col-sm-12 col-md-4 toolbar5'><'col-sm-12 col-md-4'l><'col-sm-12 col-md-4'p>>"
										+ "<'row'<'col-sm-12'tr>>",
								"pageLength" : 5,
								"language" : {
									"paginate" : {
										"previous" : "Anterior",
										"next" : "Siguiente"
									},
									"lengthMenu" : 'Mostrar <select>'
											+ '<option value="5">5</option>'
											+ '<option value="10">10</option>'
											+ '<option value="20">20</option>'
											+ '<option value="-1">Todo</option>'
											+ '</select> registros'
								}
							});
					 $("#tbpersona")
						.DataTable(
								{
									"dom" : "<'row'<'col-sm-8 col-md-4'l><'col-sm-20 col-md-4'><'col-sm-12 col-md-4'p>>"
											+ "<'row'<'col-sm-12'tr>>",
									"pageLength" : 5,
									"language" : {
										"paginate" : {
											"previous" : "Anterior",
											"next" : "Siguiente"
										},
										"lengthMenu" : 'Mostrar <select>'
												+ '<option value="5">5</option>'
												+ '<option value="10">10</option>'
												+ '<option value="20">20</option>'
												+ '<option value="-1">Todo</option>'
												+ '</select> registros'
									}
								});
					
					
					$("#tabla-sec1")
					.DataTable(
							{
								"dom" : "<'row'<'col-sm-12 col-md-4 toolbar3'>>",
								"ordering": false
							});
					
					$("#tabla-sec2")
					.DataTable(
							{
								"dom" : "<'row'<'col-sm-12 col-md-4 toolbar4'>>",
								"ordering": false
							});
					
					$("#tabla-sec3")
					.DataTable(
							{
								"dom" : "<'row'<'col-sm-12 col-md-4 toolbar4'>>",
								"ordering": false
							});
					
					$("div.toolbar5").html('<b>Usuarios</b>');
					$("div.toolbar1").html('<b>Demandantes</b>');
					$("div.toolbar2").html('<b>Demandados</b>');
					$("div.toolbar3").html('<b>Demandante</b>');
					$("div.toolbar4").html('<b>Demandado</b>');
					
				});

/*
$("#password").on("click", function() {
						
					    $(this).prop("disabled", true);
					});
var password = document.getElementById("password"),confirmPassword = document.getElementById("confirmPassword");
password.onchange = validatePassword;
confirmPassword.onkeyup = validatePassword;

function validatePassword(){
	if(password.value != confirmPassword.value) {
		  confirmPassword.setCustomValidity("Contraseñas no coinciden");
		} else {
		  confirmPassword.setCustomValidity('');
	}
}*/
