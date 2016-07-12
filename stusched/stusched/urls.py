from django.conf.urls import include, url
from django.contrib import admin
from django.views.generic import RedirectView

urlpatterns = [
    url(r'^$', RedirectView.as_view(url='/app/', permanent=False), name='index'),
    url(r'^app/', include('app.urls')),
    url(r'^admin/', include(admin.site.urls)),
]
