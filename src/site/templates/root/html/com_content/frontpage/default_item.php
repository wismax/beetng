<?php 

defined('_JEXEC') or die('Restricted access'); 

if ($this->user->authorize('com_content', 'edit', 'content', 'all') 
	|| $this->user->authorize('com_content', 'edit', 'content', 'own')) : 
	
	?><div class="contentpaneopen_edit<?php echo $this->item->params->get( 'pageclass_sfx' ); ?>" style="float: left;"><?php 
		echo JHTML::_('icon.edit', $this->item, $this->item->params, $this->access); 
	?></div><?php 

endif; ?>

<div class="contentpaneopen<?php echo $this->item->params->get( 'pageclass_sfx' ); ?>"><?php 
	if ($this->item->params->get('show_title')) : ?>
		<h2 class="contentheading<?php echo $this->item->params->get( 'pageclass_sfx' ); ?>"><?php 
		if ($this->item->params->get('link_titles') && $this->item->readmore_link != '') : 
			?><a href="<?php echo $this->item->readmore_link; ?>" class="contentpagetitle<?php echo $this->item->params->get( 'pageclass_sfx' ); ?>"><?php 
			echo $this->escape($this->item->title); 
			?></a><?php 
		else :
			echo $this->escape($this->item->title); 
		endif; 
		?></h2><?php 
	endif; 
	
	if (!$this->item->params->get('show_intro')) :
		echo $this->item->event->afterDisplayTitle;
	endif;

	if (($this->item->params->get('show_create_date'))
		|| (($this->item->params->get('show_author')) && ($this->item->author != ""))
		|| (($this->item->params->get('show_section') && $this->item->sectionid) || ($this->item->params->get('show_category') && $this->item->catid))
		|| ($this->item->params->get('show_pdf_icon') || $this->item->params->get('show_print_icon') || $this->item->params->get('show_email_icon'))
		|| ($this->item->params->get('show_url') && $this->item->urls)) :
		?><div class="article-tools">
		
			<!-- BEGIN ARTICLE META -->
			<div class="article-meta"><?php 

			if ($this->item->params->get('show_create_date')) : 
				?><span class="createdate"><?php 
				echo JHTML::_('date', $this->item->created, JText::_('DATE_FORMAT_LC2')); 
				?></span><?php 
			endif; 

			if (($this->item->params->get('show_author')) && ($this->item->author != "")) : 
				?><span class="createby"><?php 
				JText::printf(($this->item->created_by_alias ? $this->item->created_by_alias : $this->item->author) ); 
				?></span><?php 
			endif; 

			if (($this->item->params->get('show_section') && $this->item->sectionid) || ($this->item->params->get('show_category') && $this->item->catid)) : 
		
				if ($this->item->params->get('show_section') && $this->item->sectionid && isset($this->item->section)) : 
					?><span class="article-section"><?php 
					if ($this->item->params->get('link_section')) : 
						echo '<a href="'.JRoute::_(ContentHelperRoute::getSectionRoute($this->item->sectionid)).'">';
					endif;
					echo $this->item->section;
					if ($this->item->params->get('link_section')) :
						echo '</a>';
					endif;
					if ($this->item->params->get('show_category')) :
						echo ' - ';
					endif; 
					?></span><?php 
				endif;

				if ($this->item->params->get('show_category') && $this->item->catid) : 
					?><span class="article-section"><?php 
					if ($this->item->params->get('link_category')) : 
						echo '<a href="'.JRoute::_(ContentHelperRoute::getCategoryRoute($this->item->catslug, $this->item->sectionid)).'">'; 
					endif;
					echo $this->item->category;
					if ($this->item->params->get('link_section')) :
						echo '</a>';
					endif; 
					?></span><?php 
				endif;

			endif; 

			?></div>
			<!-- END ARTICLE META -->
<?php 
			if ($this->item->params->get('show_pdf_icon') || $this->item->params->get('show_print_icon') || $this->item->params->get('show_email_icon')) : 
				?><div class="buttonheading"><?php 
				if ($this->item->params->get('show_email_icon')) : 
					?><span><?php 
					echo JHTML::_('icon.email', $this->item, $this->item->params, $this->access); 
					?></span><?php 
				endif; 

				if ( $this->item->params->get( 'show_print_icon' )) : 
					?><span><?php 
					echo JHTML::_('icon.print_popup', $this->item, $this->item->params, $this->access); 
					?></span><?php 
				endif;
		
				if ($this->item->params->get('show_pdf_icon')) : 
					?><span><?php 
					echo JHTML::_('icon.pdf', $this->item, $this->item->params, $this->access); 
					?></span><?php 
				endif; 
				?></div><?php 
			endif;

			if ($this->item->params->get('show_url') && $this->item->urls) : 
				?><span class="article-url">
				<a href="http://<?php echo $this->item->urls ; ?>" target="_blank"><?php 
				echo $this->item->urls; 
				?></a>
				</span><?php 
			endif; 

		?></div>
		<!-- END ARTICAL TOOLS --><?php 
	endif; 

	echo $this->item->event->beforeDisplayContent; ?>

	<div class="article-content"><?php 
		if (isset ($this->item->toc)) :
			echo $this->item->toc;
		endif;
		echo $this->item->text; 
	?></div>

<?php 
	if ( intval($this->item->modified) != 0 && $this->item->params->get('show_modify_date')) : 
		?><span class="modifydate"><?php 
		echo JText::sprintf('LAST_UPDATED2', JHTML::_('date', $this->item->modified, JText::_('DATE_FORMAT_LC2'))); 
		?></span><?php 
	endif; 

	if ($this->item->params->get('show_readmore') && $this->item->readmore) : 
		?><a href="<?php echo $this->item->readmore_link; ?>" title="<?php echo $this->item->title; ?>" class="readon<?php echo $this->item->params->get('pageclass_sfx'); ?>"><?php 
		if ($this->item->readmore_register) :
			echo JText::_('Register to read more...');
		else :
			echo JText::_('Read more...');
		endif; 
		?></a><?php 
	endif; ?>

</div><?php 

echo $this->item->event->afterDisplayContent; ?>