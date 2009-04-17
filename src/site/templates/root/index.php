<?php
// no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<?php echo $this->language; ?>" lang="<?php echo $this->language; ?>">
	<head>
		<jdoc:include type="head" />
		<link rel="stylesheet" href="<?php echo $tmpTools->baseurl(); ?>templates/system/css/system.css" type="text/css" />
		<link rel="stylesheet" href="<?php echo $tmpTools->baseurl(); ?>templates/system/css/general.css" type="text/css" />
		<link rel="stylesheet" href="<?php echo $tmpTools->templateurl(); ?>/css/template.css" type="text/css" />
		<!--[if IE]><link rel="stylesheet" type="text/css" href="<?php echo $tmpTools->templateurl(); ?>/css/template_ie.css" /><![endif]-->
		<!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="<?php echo $tmpTools->templateurl(); ?>/css/template_ie_6.css" /><![endif]-->
	</head>
	<body>
		<div class="sky"></div>
		<div class="earth">
			<div id="rootContainer">
				<div id="rootHeader">
					<h1>beet</h1>
				</div>
				<div id="rootContent">
				</div>
				<div id="rootFooter">
				</div>
			</div>
		</div>
		<div class="rock"></div>
	</body>
</html>
