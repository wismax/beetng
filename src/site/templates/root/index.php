<?php
	defined( '_JEXEC' ) or die( 'Restricted access' );
	$config = new JConfig();
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<?php echo $this->language; ?>" lang="<?php echo $this->language; ?>">
	<head>
		<jdoc:include type="head" />
		<link rel="stylesheet" href="<?php echo JURI::base(); ?>templates/system/css/system.css" type="text/css" />
		<link rel="stylesheet" href="<?php echo JURI::base(); ?>templates/system/css/general.css" type="text/css" />
		<link rel="stylesheet" href="<?php echo JURI::base(); ?>templates/root/css/template.css" type="text/css" />
		<!--[if IE]><link rel="stylesheet" type="text/css" href="<?php echo JURI::base(); ?>templates/root/css/template_ie.css" /><![endif]-->
		<!--[if lt IE 7]><link rel="stylesheet" type="text/css" href="<?php echo JURI::base(); ?>templates/root/css/template_ie_6.css" /><![endif]-->
	</head>
	<body>

		<script type="text/javascript">
		var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
		document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
		try {
		var pageTracker = _gat._getTracker("UA-8485662-1");
		pageTracker._trackPageview();
		} catch(err) {}</script>

		<div class="sky"></div>
		<div class="earth">
			
			<div id="rootContainer">
			<div id="rootOutline">

				<!-- BEGIN HEADER -->
				<div id="rootHeader"><?php

					$siteName = $config->sitename;
					$logoClass = 'logo';

					$logoText = trim($this->params->get('logoText'));
					if ($logoText == ''):
						$logoText = $siteName;
					endif;

					if ($this->params->get('logoType')!='image'): 
						$logoClass = 'logo-text';
					endif;

					?><div id="rootHeaderLeft">
						<h1 class="<?php echo $logoClass; ?>">
							<a href="<?php echo JURI::base(); ?>" title="<?php echo $siteName; ?>"><?php echo $logoText; ?></a>
						</h1>
						<?php if(JRequest::getCmd( 'view' ) != 'frontpage') : ?>
						<div id="rootBreadcrumbs">
							<jdoc:include type="module" name="breadcrumbs" />
						</div>
						<?php endif ; ?>
					</div>
					<?php if ($this->countModules('top')): ?>
					<!-- BEGIN: TOP -->
					<div id="rootTop">
						<jdoc:include type="modules" name="top" style="xhtml" />
					</div>
					<!-- END: TOP -->
					<?php endif; ?>
				</div>
				<!-- END HEADER -->
				
				<!-- BEGIN CENTER -->
				<div id="rootCenter">

					<?php if ($this->countModules('left')): 
					?><!-- BEGIN: LEFT COLUMN -->
					<div id="rootLeft">
						<jdoc:include type="modules" name="left" style="xhtml" />
						<!--[if IE]><div class="minwidth"></div><![endif]-->
					</div>
					<!-- END: LEFT COLUMN -->
					<?php endif; ?>

					<?php if ($this->countModules('right')): ?>
					<!-- BEGIN: RIGHT COLUMN -->
					<div id="rootRight">
						<jdoc:include type="modules" name="right" style="xhtml" />
					</div>
					<!-- END: RIGHT COLUMN -->
					<?php endif; ?>
				
					<!-- BEGIN: CONTENT -->
					<div id="rootContent">
					
						<?php if (strpos($_SERVER['HTTP_USER_AGENT'], 'MSIE')):
						?><!--[if IE]>
							<div class="ieTaunt">
							<h3>Greetings IE user!</h3>  The view is <b>much nicer</b> from one of these browsers:
							<ul>
								<li><a href="http://www.mozilla.com/firefox"><img border="0" alt="Firefox 3" title="Firefox 3" src="<?php echo JURI::base(); ?>templates/root/images/firefox.png"/> Firefox</a></li>
								<li><a href="http://www.apple.com/safari"><img border="0" alt="Safari 4" title="Safari 4" src="<?php echo JURI::base(); ?>templates/root/images/safari.png"/> Safari</a></li>
								<li><a href="http://www.google.com/chrome"><img border="0" alt="Google Chrome" title="Google Chrome" src="<?php echo JURI::base(); ?>templates/root/images/chrome.png"/> Chrome</a></li>
							</ul>
							</div>
							<div class="ieContent">
						<![endif]-->
						<?php endif; ?>
						
						<jdoc:include type="message" />
						<jdoc:include type="component" />
				
						<?php if (strpos($_SERVER['HTTP_USER_AGENT'], 'MSIE')):
						?><!--[if IE]></div><![endif]-->
						<?php endif; ?>
					</div>
					<!-- END: CONTENT -->
					
				</div>
				<!-- END CENTER -->

				<!-- BEGIN FOOTER -->				
				<div id="rootFooter">
					<a id="sfLogo" href="http://sourceforge.net/projects/beet">
						<img src="http://sflogo.sourceforge.net/sflogo.php?group_id=258926&amp;type=14" 
							 alt="Get beet at SourceForge.net. Fast, secure and Free Open Source software downloads" width="150" height="40"/>
						<!--[if IE]><div class="minwidth"></div><![endif]-->
					</a>
					<div class="copyright">
						<jdoc:include type="modules" name="footer" />
					</div>
				</div>
				<!-- END FOOTER -->
				
			</div>
			</div>
			
		</div>
		<div class="rock"><jdoc:include type="modules" name="debug" /></div>
	</body>
</html>
