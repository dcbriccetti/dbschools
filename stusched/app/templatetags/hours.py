from django import template

register = template.Library()

@register.filter
def hours(duration):
    return '%.2g' % (duration.seconds / 60 / 60)
